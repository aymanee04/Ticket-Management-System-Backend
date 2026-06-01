package ma.bank.ticketmanagementsystembackend.services;

import lombok.RequiredArgsConstructor;
import ma.bank.ticketmanagementsystembackend.dtos.dto.TicketDTO;
import ma.bank.ticketmanagementsystembackend.entities.*;
import ma.bank.ticketmanagementsystembackend.exceptions.BusinessException;
import ma.bank.ticketmanagementsystembackend.exceptions.ResourceNotFoundException;
import ma.bank.ticketmanagementsystembackend.mappers.TicketMapper;
import ma.bank.ticketmanagementsystembackend.repositories.ClientRepository;
import ma.bank.ticketmanagementsystembackend.repositories.TicketRepository;
import ma.bank.ticketmanagementsystembackend.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private static final int SOFT_DELETE_DAYS = 5;

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final TicketMapper ticketMapper;
    private final EmailService emailService;
    //  Create

    @Override
    @Transactional
    public TicketDTO createTicket(String title, String description, Long createdById, Long clientId) {
        AppUser creator = findUserById(createdById, "Creator not found");

        if (creator.getClient() != null && creator.getClient().getStatus() != ClientStatus.ACTIVE) {
            throw new BusinessException(
                    "Cannot create tickets. Company account is " + creator.getClient().getStatus());
        }

        Client client = findClientById(clientId);

        if (client.getStatus() != ClientStatus.ACTIVE) {
            throw new BusinessException(
                    "Cannot create ticket for non active client: " + client.getName());
        }

        Ticket ticket = Ticket.builder()
                .title(title)
                .description(description)
                .status(TicketStatus.IN_PROGRESS)
                .createdAt(LocalDateTime.now())
                .createdBy(creator)
                .clients(Set.of(client))
                .build();

        Ticket saved = ticketRepository.save(ticket);
        emailService.sendTicketCreatedEmail(saved);
        return ticketMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public TicketDTO createMultiClientTicket(String title, String description,
                                             Long createdById, Set<Long> clientIds) {
        AppUser creator = findUserById(createdById, "Creator not found");

        if (clientIds == null || clientIds.isEmpty()) {
            throw new BusinessException("At least one client must be specified");
        }

        Set<Client> clients = new HashSet<>();
        for (Long clientId : clientIds) {
            Client client = clientRepository.findById(clientId)
                    .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + clientId));

            if (client.getStatus() != ClientStatus.ACTIVE) {
                throw new BusinessException(
                        "Cannot create ticket non active client " + client.getName());
            }
            clients.add(client);
        }

        Ticket ticket = Ticket.builder()
                .title(title)
                .description(description)
                .status(TicketStatus.IN_PROGRESS)
                .createdAt(LocalDateTime.now())
                .createdBy(creator)
                .clients(clients)
                .build();

        Ticket saved = ticketRepository.save(ticket);
        emailService.sendIncidentNotification(saved);
        return ticketMapper.toDTO(saved);
    }

    // Ticket Lifecycle

    @Override
    @Transactional
    public TicketDTO assignTicket(Long ticketId, Long assignedToId) {
        Ticket ticket = findTicketById(ticketId);
        AppUser assignee = findUserById(assignedToId, "Assignee not found");
        boolean isAdmin = assignee.getRoles().contains(Role.ADMIN);
        if (!isAdmin) {
            if (assignee.getClient() == null ||
                    !assignee.getClient().equals(ticket.getCreatedBy().getClient())) {
                throw new BusinessException(
                        "Assignee must belong to the same client as the ticket creator");
            }
        }
        requireValidatableStatus(ticket, "assigned");

        ticket.setAssignedTo(assignee);
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket saved = ticketRepository.save(ticket);
        emailService.sendTicketAssignedEmail(saved, assignee);
        return ticketMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public TicketDTO approveTicket(Long ticketId, Long managerId, String comment) {
        Ticket ticket = findTicketById(ticketId);
        AppUser manager = findUserById(managerId, "Manager not found");

        requireValidatableStatus(ticket, "approved");

        ticket.setStatus(TicketStatus.VALIDATED);
        ticket.setValidatedAt(LocalDateTime.now());
        ticket.setValidatedBy(manager);
        ticket.setValidationComment(comment);
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket saved = ticketRepository.save(ticket);
        emailService.sendTicketValidatedEmail(saved);
        return ticketMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public TicketDTO rejectTicket(Long ticketId, Long managerId, String comment) {
        Ticket ticket = findTicketById(ticketId);
        AppUser manager = findUserById(managerId, "Manager not found");
        requireValidatableStatus(ticket, "rejected");

        ticket.setStatus(TicketStatus.REJECTED);
        ticket.setValidatedAt(LocalDateTime.now());
        ticket.setValidatedBy(manager);
        ticket.setValidationComment(comment);
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket saved = ticketRepository.save(ticket);
        emailService.sendTicketRejectedEmail(saved);
        return ticketMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public TicketDTO archiveTicket(Long ticketId) {
        Ticket ticket = findTicketById(ticketId);
        if (ticket.getStatus() != TicketStatus.VALIDATED &&
                ticket.getStatus() != TicketStatus.REJECTED) {
            throw new BusinessException(
                    "Only VALIDATED or REJECTED tickets can be archived");
        }
        ticket.setStatus(TicketStatus.ARCHIVED);
        ticket.setUpdatedAt(LocalDateTime.now());
        return ticketMapper.toDTO(ticketRepository.save(ticket));
    }

    @Override
    @Transactional
    public TicketDTO cancelTicket(Long ticketId) {
        Ticket ticket = findTicketById(ticketId);
        requireValidatableStatus(ticket, "cancelled");
        ticket.setStatus(TicketStatus.CANCELLED);
        ticket.setUpdatedAt(LocalDateTime.now());
//        ticket.setScheduledDeleteAt(LocalDateTime.now().plusDays(SOFT_DELETE_DAYS));

        return ticketMapper.toDTO(ticketRepository.save(ticket));
    }

    //  Delete
///
    @Override
    @Transactional
    public void hardDeleteTicket(Long ticketId) {
        Ticket ticket = findTicketById(ticketId);

        if (ticket.getStatus() != TicketStatus.CANCELLED) {
            throw new BusinessException(
                    "Only CANCELLED tickets can be permanently deleted");
        }

        ticketRepository.delete(ticket);
    }

    //  Read
///
    @Override
    public TicketDTO getTicketById(Long id, AppUser currentUser) {
        Ticket ticket = findTicketById(id);
        Collection<Role> roles = currentUser.getRoles();

        if (roles.contains(Role.ADMIN)) {
            return ticketMapper.toDTO(ticket);
        }

        if (roles.contains(Role.MANAGER) && currentUser.getClient() != null) {
            boolean sameClient = ticket.getClients().stream()
                    .anyMatch(client ->
                            client.getClientId().equals(currentUser.getClient().getClientId())
                    );
            if (sameClient) {
                return ticketMapper.toDTO(ticket);
            }
        }

        if (ticket.getCreatedBy() != null &&
                ticket.getCreatedBy().getUserId().equals(currentUser.getUserId())) {
            return ticketMapper.toDTO(ticket);
        }
        throw new BusinessException("You are not allowed to access this ticket");
    }

    @Override
    public List<TicketDTO> getAllTickets() {
        return ticketRepository.findAll().stream().map(ticketMapper::toDTO).toList();
    }

    @Override
    public List<TicketDTO> getTicketsByStatus(TicketStatus status) {
        return ticketRepository.findByStatus(status).stream().map(ticketMapper::toDTO).toList();
    }

    @Override
    public List<TicketDTO> getTicketsByUserId(Long userId) {
        return ticketRepository.findTicketByCreatedBy_UserId(userId)
                .stream().map(ticketMapper::toDTO).toList();
    }

    @Override
    public List<TicketDTO> getTicketsByClient(Long clientId) {
        return ticketRepository.findByClients_ClientId(clientId)
                .stream().map(ticketMapper::toDTO).toList();
    }

    @Override
    public List<TicketDTO> getTicketsAssignedTo(Long userId) {
        AppUser user = findUserById(userId, "User not found");
        return ticketRepository.findByAssignedTo(user).stream().map(ticketMapper::toDTO).toList();
    }

    @Override
    public List<TicketDTO> searchTickets(String searchTerm) {
        return ticketRepository.searchTickets(searchTerm).stream().map(ticketMapper::toDTO).toList();
    }

    // ── Read — paginated ──────────────────────────────────────────────────────

    @Override
    public Page<TicketDTO> getAllTicketsWithPagination(Pageable pageable) {
        return ticketRepository.findAll(pageable).map(ticketMapper::toDTO);
    }

    @Override
    public Page<TicketDTO> getTicketsByStatusWithPagination(TicketStatus status, Pageable pageable) {
        return ticketRepository.findByStatus(status, pageable).map(ticketMapper::toDTO);
    }

    @Override
    public Page<TicketDTO> searchTicketsWithPagination(String searchTerm, Pageable pageable) {
        return ticketRepository.searchTickets(searchTerm, pageable).map(ticketMapper::toDTO);
    }

    @Override
    public Page<TicketDTO> getTicketsByClientId(Long clientId, Pageable pageable) {
        return ticketRepository.findByClients_ClientId(clientId, pageable).map(ticketMapper::toDTO);
    }

    @Override
    public Page<TicketDTO> searchTicketsByClient(String searchTerm, Long clientId, Pageable pageable) {
        return ticketRepository.searchTicketsByClient(searchTerm, clientId, pageable)
                .map(ticketMapper::toDTO);
    }

    @Override
    public Page<TicketDTO> getTicketsByStatusAndClient(TicketStatus status, Long clientId, Pageable pageable) {
        return ticketRepository.findByStatusAndClients_ClientId(status, clientId, pageable)
                .map(ticketMapper::toDTO);
    }

    //  Role-scoped paginated
///
    @Override
    public Page<TicketDTO> getTicketsPagedForUser(AppUser currentUser, Pageable pageable) {
        if (isAdminOrManager(currentUser)) {
            return getAllTicketsWithPagination(pageable);
        }
        if (currentUser.getClient() != null) {
            return getTicketsByClientId(currentUser.getClient().getClientId(), pageable);
        }
        return Page.empty(pageable);
    }
///
    @Override
    public Page<TicketDTO> getTicketsByStatusPagedForUser(TicketStatus status, AppUser currentUser, Pageable pageable) {
        if (isAdminOrManager(currentUser)) {
            return getTicketsByStatusWithPagination(status, pageable);
        }
        if (currentUser.getClient() != null) {
            return getTicketsByStatusAndClient(status, currentUser.getClient().getClientId(), pageable);
        }
        throw new BusinessException("You are not allowed to access those tickets");
    }
///
    @Override
    public Page<TicketDTO> searchTicketsPagedForUser(String q, AppUser currentUser, Pageable pageable) {
        if (isAdminOrManager(currentUser)) {
            return searchTicketsWithPagination(q, pageable);
        }
        if (currentUser.getClient() != null) {
            return searchTicketsByClient(q, currentUser.getClient().getClientId(), pageable);
        }
        return Page.empty(pageable);
    }

    //  Private helpers

    private Ticket findTicketById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
    }

    private AppUser findUserById(Long id, String message) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(message));
    }

    private Client findClientById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
    }

    private void requireValidatableStatus(Ticket ticket, String action) {
        if (ticket.getStatus() != TicketStatus.IN_PROGRESS &&
                ticket.getStatus() != TicketStatus.PENDING_VALIDATION) {
            throw new BusinessException(
                    "Only IN_PROGRESS or PENDING_VALIDATION tickets can be " + action);
        }
    }
///
    private boolean isAdminOrManager(AppUser user) {
        return user.getRoles().contains(Role.ADMIN) ||
                user.getRoles().contains(Role.MANAGER);
    }
}
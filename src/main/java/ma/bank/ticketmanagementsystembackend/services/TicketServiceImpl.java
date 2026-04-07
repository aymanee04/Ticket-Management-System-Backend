package ma.bank.ticketmanagementsystembackend.services;

import lombok.AllArgsConstructor;
import ma.bank.ticketmanagementsystembackend.dtos.dto.TicketDTO;
import ma.bank.ticketmanagementsystembackend.entities.*;
import ma.bank.ticketmanagementsystembackend.mappers.TicketMapper;
import ma.bank.ticketmanagementsystembackend.repositories.ClientRepository;
import ma.bank.ticketmanagementsystembackend.repositories.TicketRepository;
import ma.bank.ticketmanagementsystembackend.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final TicketMapper ticketMapper;
    private final EmailService emailService;

    @Override
    @Transactional
    public TicketDTO createTicket(String title, String description, Long createdById, Long clientId) {
        AppUser creator = userRepository.findById(createdById)
                .orElseThrow(() -> new RuntimeException("Creator not found"));

        if (creator.getClient() != null && creator.getClient().getStatus() != ClientStatus.ACTIVE) {
            throw new RuntimeException(
                    "Cannot create tickets. Company account is " +
                            creator.getClient().getStatus()
            );
        }

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        if (client.getStatus() != ClientStatus.ACTIVE) {
            throw new RuntimeException("Cannot create ticket for inactive client: " + client.getName());
        }

        Ticket ticket = Ticket.builder()
                .title(title)
                .description(description)
                .status(TicketStatus.IN_PROGRESS)
                .createdAt(LocalDateTime.now())
                .createdBy(creator)
                .clients(Set.of(client))
                .build();

        Ticket savedTicket = ticketRepository.save(ticket);
        emailService.sendTicketCreatedEmail(savedTicket);
        return ticketMapper.toDTO(savedTicket);
    }

    @Override
    @Transactional
    public TicketDTO createMultiClientTicket(String title, String description, Long createdById, Set<Long> clientIds) {
        AppUser creator = userRepository.findById(createdById)
                .orElseThrow(() -> new RuntimeException("Creator not found"));

        if (clientIds == null || clientIds.isEmpty()) {
            throw new RuntimeException("At least one client must be specified");
        }

        Set<Client> clients = new HashSet<>();
        for (Long clientId : clientIds) {
            Client client = clientRepository.findById(clientId)
                    .orElseThrow(() -> new RuntimeException("Client not found: " + clientId));

            if (client.getStatus() != ClientStatus.ACTIVE) {
                throw new RuntimeException("Cannot create ticket for inactive client: " + client.getName());
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

        Ticket savedTicket = ticketRepository.save(ticket);
        emailService.sendIncidentNotification(savedTicket);
        return ticketMapper.toDTO(savedTicket);
    }

    @Override
    @Transactional
    public TicketDTO assignTicket(Long ticketId, Long assignedToId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        AppUser assignee = userRepository.findById(assignedToId)
                .orElseThrow(() -> new RuntimeException("Assignee not found"));

        ticket.setAssignedTo(assignee);
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket assignedTicket = ticketRepository.save(ticket);
        emailService.sendTicketAssignedEmail(assignedTicket, assignee);
        return ticketMapper.toDTO(assignedTicket);
    }

    @Transactional
    @Override
    public TicketDTO approveTicket(Long ticketId, Long managerId, String comment) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        AppUser manager = userRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        if (ticket.getStatus() != TicketStatus.IN_PROGRESS &&
                ticket.getStatus() != TicketStatus.PENDING_VALIDATION) {
            throw new RuntimeException("Only IN_PROGRESS or PENDING_VALIDATION tickets can be approved");
        }

        ticket.setStatus(TicketStatus.VALIDATED);
        ticket.setValidatedAt(LocalDateTime.now());
        ticket.setValidatedBy(manager);
        ticket.setValidationComment(comment);
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket approvedTicket = ticketRepository.save(ticket);
        emailService.sendTicketValidatedEmail(approvedTicket);
        return ticketMapper.toDTO(approvedTicket);
    }

    @Override
    @Transactional
    public TicketDTO rejectTicket(Long ticketId, Long managerId, String comment) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        AppUser manager = userRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        if (ticket.getStatus() != TicketStatus.IN_PROGRESS &&
                ticket.getStatus() != TicketStatus.PENDING_VALIDATION) {
            throw new RuntimeException("Only IN_PROGRESS or PENDING_VALIDATION tickets can be rejected");
        }

        ticket.setStatus(TicketStatus.REJECTED);
        ticket.setValidatedAt(LocalDateTime.now());
        ticket.setValidatedBy(manager);
        ticket.setValidationComment(comment);
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket rejectedTicket = ticketRepository.save(ticket);
        emailService.sendTicketRejectedEmail(rejectedTicket);
        return ticketMapper.toDTO(rejectedTicket);
    }

    @Override
    @Transactional
    public TicketDTO archiveTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (ticket.getStatus() != TicketStatus.VALIDATED &&
                ticket.getStatus() != TicketStatus.REJECTED) {
            throw new RuntimeException("Only VALIDATED or REJECTED tickets can be archived");
        }

        ticket.setStatus(TicketStatus.ARCHIVED);
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket archivedTicket = ticketRepository.save(ticket);
        return ticketMapper.toDTO(archivedTicket);
    }

    @Override
    @Transactional
    public TicketDTO cancelTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (ticket.getStatus() != TicketStatus.IN_PROGRESS &&
                ticket.getStatus() != TicketStatus.PENDING_VALIDATION) {
            throw new RuntimeException("Only IN_PROGRESS or PENDING_VALIDATION tickets can be cancelled");
        }

        ticket.setStatus(TicketStatus.CANCELLED);
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket canceledTicket = ticketRepository.save(ticket);
        return ticketMapper.toDTO(canceledTicket);
    }

    @Override
    public TicketDTO getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        return ticketMapper.toDTO(ticket);
    }

    @Override
    public List<TicketDTO> getAllTickets() {
        return ticketRepository.findAll().stream().map(ticketMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<TicketDTO> getTicketsByStatus(TicketStatus status) {
        return ticketRepository.findByStatus(status).stream().map(ticketMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<TicketDTO> getTicketsByUserId(Long userId) {
        return ticketRepository.findTicketByCreatedBy_UserId(userId).stream().map(ticketMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<TicketDTO> getTicketsByClient(Long clientId) {
        return ticketRepository.findByClients_ClientId(clientId).stream().map(ticketMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<TicketDTO> getTicketsAssignedTo(Long userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ticketRepository.findByAssignedTo(user).stream().map(ticketMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<TicketDTO> searchTickets(String searchTerm) {
        return ticketRepository.searchTickets(searchTerm).stream()
                .map(ticketMapper::toDTO)
                .collect(Collectors.toList());
    }

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
        return ticketRepository.findByClients_ClientId(clientId, pageable)
                .map(ticketMapper::toDTO);
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
}
package ma.bank.ticketmanagementsystembackend.services;

import ma.bank.ticketmanagementsystembackend.dtos.dto.TicketDTO;
import ma.bank.ticketmanagementsystembackend.entities.AppUser;
import ma.bank.ticketmanagementsystembackend.entities.Ticket;
import ma.bank.ticketmanagementsystembackend.entities.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface TicketService {
    public TicketDTO createTicket(String title, String description, Long createdById, Long clientId);
    public TicketDTO createMultiClientTicket(String title, String description, Long createdById, Set<Long> clientIds);
    public TicketDTO assignTicket(Long ticketId, Long assignedToId);
    public TicketDTO approveTicket(Long ticketId,Long adminId, String comment);
    public TicketDTO rejectTicket(Long ticketId,Long adminId, String comment);
    public TicketDTO archiveTicket(Long ticketId);
    public TicketDTO cancelTicket(Long ticketId);
    public void hardDeleteTicket(Long ticketId);
    public TicketDTO getTicketById(Long id, AppUser currentUser);
    public List<TicketDTO> getAllTickets();
    public List<TicketDTO> getTicketsByStatus(TicketStatus status);
    public List<TicketDTO> getTicketsByUserId(Long userId);
    public List<TicketDTO> getTicketsByClient(Long clientId);
    public List<TicketDTO> getTicketsAssignedTo(Long userId);
    List<TicketDTO> searchTickets(String searchTerm);

    Page<TicketDTO> getAllTicketsWithPagination(Pageable pageable);
    Page<TicketDTO> getTicketsByStatusWithPagination(TicketStatus status, Pageable pageable);
    Page<TicketDTO> searchTicketsWithPagination(String searchTerm, Pageable pageable);

    Page<TicketDTO> getTicketsByClientId(Long clientId, Pageable pageable);
    Page<TicketDTO> searchTicketsByClient(String searchTerm, Long clientId, Pageable pageable);
    Page<TicketDTO> getTicketsByStatusAndClient(TicketStatus status, Long clientId, Pageable pageable);
    public Page<TicketDTO> getTicketsPagedForUser(AppUser currentUser, Pageable pageable);
    public Page<TicketDTO> getTicketsByStatusPagedForUser(TicketStatus status, AppUser currentUser, Pageable pageable);
    public Page<TicketDTO> searchTicketsPagedForUser(String q, AppUser currentUser, Pageable pageable);
}

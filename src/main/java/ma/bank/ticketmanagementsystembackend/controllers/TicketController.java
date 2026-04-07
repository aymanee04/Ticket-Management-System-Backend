package ma.bank.ticketmanagementsystembackend.controllers;

import ma.bank.ticketmanagementsystembackend.dtos.*;
import ma.bank.ticketmanagementsystembackend.dtos.dto.TicketDTO;
import ma.bank.ticketmanagementsystembackend.entities.AppUser;
import ma.bank.ticketmanagementsystembackend.entities.Role;
import ma.bank.ticketmanagementsystembackend.entities.TicketStatus;
import ma.bank.ticketmanagementsystembackend.repositories.UserRepository;
import ma.bank.ticketmanagementsystembackend.services.TicketService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin("*")
public class TicketController {
    private final TicketService ticketService;
    private final UserRepository userRepository;

    public TicketController(TicketService ticketService, UserRepository userRepository) {
        this.ticketService = ticketService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<TicketDTO> createTicket(@RequestBody CreateTicketRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AppUser currentUser = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (currentUser.getClient() == null) {
            throw new RuntimeException("User must belong to a client to create tickets");
        }

        TicketDTO ticket = ticketService.createTicket(
                request.getTitle(),
                request.getDescription(),
                currentUser.getUserId(),
                currentUser.getClient().getClientId()
        );

        return ResponseEntity.ok(ticket);
    }

    @PostMapping("/incident")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<TicketDTO> createIncident(@RequestBody CreateIncidentRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AppUser currentUser = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getClientIds() == null || request.getClientIds().isEmpty()) {
            throw new RuntimeException("At least one client must be selected");
        }

        TicketDTO ticket = ticketService.createMultiClientTicket(
                request.getTitle(),
                request.getDescription(),
                currentUser.getUserId(),
                request.getClientIds()
        );

        return ResponseEntity.ok(ticket);
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<TicketDTO> assignTicket(
            @PathVariable Long id,
            @RequestBody AssignTicketRequest request) {
        return ResponseEntity.ok(ticketService.assignTicket(id, request.getAssignedToId()));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<TicketDTO> approveTicket(
            @PathVariable Long id,
            @RequestParam Long managerId,
            @RequestBody Map<String, String> request) {

        String comment = request.get("comment");
        return ResponseEntity.ok(ticketService.approveTicket(id, managerId, comment));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<TicketDTO> rejectTicket(
            @PathVariable Long id,
            @RequestParam Long managerId,
            @RequestBody Map<String, String> request) {
        String comment = request.get("comment");
        return ResponseEntity.ok(ticketService.rejectTicket(id, managerId, comment));
    }

    @PutMapping("/{id}/archive")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<TicketDTO> archiveTicket(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.archiveTicket(id));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<TicketDTO> cancelTicket(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.cancelTicket(id));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('USER')")
    public ResponseEntity<TicketDTO> getTicket(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getTicketById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('USER')")
    public ResponseEntity<List<TicketDTO>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyAuthority('USER')")
    public ResponseEntity<List<TicketDTO>> getTicketsByStatus(@PathVariable TicketStatus status) {
        return ResponseEntity.ok(ticketService.getTicketsByStatus(status));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyAuthority('USER')")
    public ResponseEntity<List<TicketDTO>> getTicketsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(ticketService.getTicketsByUserId(userId));
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasAnyAuthority('USER')")
    public ResponseEntity<List<TicketDTO>> getTicketsByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(ticketService.getTicketsByClient(clientId));
    }

    @GetMapping("/assigned/{userId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<List<TicketDTO>> getTicketsAssignedTo(@PathVariable Long userId) {
        return ResponseEntity.ok(ticketService.getTicketsAssignedTo(userId));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('USER')")
    public ResponseEntity<List<TicketDTO>> searchTickets(@RequestParam String q) {
        return ResponseEntity.ok(ticketService.searchTickets(q));
    }


    @GetMapping(params = "page")
    @PreAuthorize("hasAnyAuthority('USER')")
    public ResponseEntity<PagedResponse<TicketDTO>> getAllTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        //########"//
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AppUser currentUser = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Page<TicketDTO> ticketPage;

        if (currentUser.getRoles().contains(Role.ADMIN) ||
                currentUser.getRoles().contains(Role.MANAGER)) {
            ticketPage = ticketService.getAllTicketsWithPagination(pageable);
        } else {
            if (currentUser.getClient() != null) {
                ticketPage = ticketService.getTicketsByClientId(
                        currentUser.getClient().getClientId(),
                        pageable
                );
            } else {
                ticketPage = Page.empty();
            }
        }

        PagedResponse<TicketDTO> response = new PagedResponse<>(
                ticketPage.getContent(),
                ticketPage.getNumber(),
                ticketPage.getSize(),
                ticketPage.getTotalElements(),
                ticketPage.getTotalPages(),
                ticketPage.isLast(),
                ticketPage.isFirst()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/status/{status}", params = "page")
    @PreAuthorize("hasAnyAuthority('USER')")
    public ResponseEntity<PagedResponse<TicketDTO>> getTicketsByStatus(
            @PathVariable TicketStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AppUser currentUser = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Page<TicketDTO> ticketPage;

        if (currentUser.getRoles().contains(Role.ADMIN) ||
                currentUser.getRoles().contains(Role.MANAGER)) {
            ticketPage = ticketService.getTicketsByStatusWithPagination(status, pageable);
        } else {
            if (currentUser.getClient() != null) {
                ticketPage = ticketService.getTicketsByStatusAndClient(
                        status,
                        currentUser.getClient().getClientId(),
                        pageable
                );
            } else {
                ticketPage = Page.empty();
            }
        }

        PagedResponse<TicketDTO> response = new PagedResponse<>(
                ticketPage.getContent(),
                ticketPage.getNumber(),
                ticketPage.getSize(),
                ticketPage.getTotalElements(),
                ticketPage.getTotalPages(),
                ticketPage.isLast(),
                ticketPage.isFirst()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/search", params = "page")
    @PreAuthorize("hasAnyAuthority('USER')")
    public ResponseEntity<PagedResponse<TicketDTO>> searchTickets(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AppUser currentUser = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Page<TicketDTO> ticketPage;

        if (currentUser.getRoles().contains(Role.ADMIN) ||
                currentUser.getRoles().contains(Role.MANAGER)) {
            ticketPage = ticketService.searchTicketsWithPagination(q, pageable);
        } else {
            if (currentUser.getClient() != null) {
                ticketPage = ticketService.searchTicketsByClient(
                        q,
                        currentUser.getClient().getClientId(),
                        pageable
                );
            } else {
                ticketPage = Page.empty();
            }
        }

        PagedResponse<TicketDTO> response = new PagedResponse<>(
                ticketPage.getContent(),
                ticketPage.getNumber(),
                ticketPage.getSize(),
                ticketPage.getTotalElements(),
                ticketPage.getTotalPages(),
                ticketPage.isLast(),
                ticketPage.isFirst()
        );

        return ResponseEntity.ok(response);
    }
}
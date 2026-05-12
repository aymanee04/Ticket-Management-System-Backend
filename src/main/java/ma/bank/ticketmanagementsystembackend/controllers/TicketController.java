package ma.bank.ticketmanagementsystembackend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.bank.ticketmanagementsystembackend.PaginationUtils;
import ma.bank.ticketmanagementsystembackend.dtos.*;
import ma.bank.ticketmanagementsystembackend.dtos.dto.TicketDTO;
import ma.bank.ticketmanagementsystembackend.entities.AppUser;
import ma.bank.ticketmanagementsystembackend.entities.TicketStatus;
import ma.bank.ticketmanagementsystembackend.exceptions.BusinessException;
import ma.bank.ticketmanagementsystembackend.services.TicketService;
import ma.bank.ticketmanagementsystembackend.services.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final UserService userService;

    //  Create

    @PostMapping
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<TicketDTO> createTicket(
            Authentication auth,
            @RequestBody @Valid CreateTicketRequest request) {

        AppUser currentUser = userService.loadUserByEmail(auth.getName());

        if (currentUser.getClient() == null) {
            throw new BusinessException("User must belong to a client to create tickets");
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ticketService.createTicket(
                        request.getTitle(),
                        request.getDescription(),
                        currentUser.getUserId(),
                        currentUser.getClient().getClientId()
                ));
    }

    @PostMapping("/incident")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<TicketDTO> createIncident(
            Authentication auth,
            @RequestBody @Valid CreateIncidentRequest request) {

        AppUser currentUser = userService.loadUserByEmail(auth.getName());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ticketService.createMultiClientTicket(
                        request.getTitle(),
                        request.getDescription(),
                        currentUser.getUserId(),
                        request.getClientIds()
                ));
    }

    //  Ticket Lifecycle

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<TicketDTO> assignTicket(
            @PathVariable Long id,
            @RequestBody @Valid AssignTicketRequest request) {
        return ResponseEntity.ok(ticketService.assignTicket(id, request.getAssignedToId()));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<TicketDTO> approveTicket(
            Authentication auth,
            @PathVariable Long id,
            @RequestBody @Valid TicketActionRequest request) {

        AppUser manager = userService.loadUserByEmail(auth.getName());
        return ResponseEntity.ok(
                ticketService.approveTicket(id, manager.getUserId(), request.getComment()));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<TicketDTO> rejectTicket(
            Authentication auth,
            @PathVariable Long id,
            @RequestBody @Valid TicketActionRequest request) {

        AppUser manager = userService.loadUserByEmail(auth.getName());
        return ResponseEntity.ok(
                ticketService.rejectTicket(id, manager.getUserId(), request.getComment()));
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

    //  Delete

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> hardDeleteTicket(@PathVariable Long id) {
        ticketService.hardDeleteTicket(id);
        return ResponseEntity.noContent().build();
    }

    //  Read

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<TicketDTO> getTicket(@PathVariable Long id,
                                                Authentication auth) {
        AppUser currentUser = userService.loadUserByEmail(auth.getName());
        return ResponseEntity.ok(ticketService.getTicketById(id, currentUser));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<List<TicketDTO>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<List<TicketDTO>> getTicketsByStatus(@PathVariable TicketStatus status) {
        return ResponseEntity.ok(ticketService.getTicketsByStatus(status));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<List<TicketDTO>> getTicketsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(ticketService.getTicketsByUserId(userId));
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<List<TicketDTO>> getTicketsByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(ticketService.getTicketsByClient(clientId));
    }

    @GetMapping("/assigned/{userId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<List<TicketDTO>> getTicketsAssignedTo(@PathVariable Long userId) {
        return ResponseEntity.ok(ticketService.getTicketsAssignedTo(userId));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<List<TicketDTO>> searchTickets(@RequestParam String q) {
        return ResponseEntity.ok(ticketService.searchTickets(q));
    }

    //  Paginated

    @GetMapping(params = "page")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<PagedResponse<TicketDTO>> getAllTicketsPaged(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Pageable pageable = PaginationUtils.buildPageable(page, size, sortBy, direction);
        AppUser currentUser = userService.loadUserByEmail(auth.getName());
        return ResponseEntity.ok(
                PaginationUtils.toPagedResponse(ticketService.getTicketsPagedForUser(currentUser, pageable)));
    }

    @GetMapping(value = "/status/{status}", params = "page")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<PagedResponse<TicketDTO>> getTicketsByStatusPaged(
            Authentication auth,
            @PathVariable TicketStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        AppUser currentUser = userService.loadUserByEmail(auth.getName());
        return ResponseEntity.ok(
                PaginationUtils.toPagedResponse(ticketService.getTicketsByStatusPagedForUser(status, currentUser, pageable)));
    }

    @GetMapping(value = "/search", params = "page")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<PagedResponse<TicketDTO>> searchTicketsPaged(
            Authentication auth,
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        AppUser currentUser = userService.loadUserByEmail(auth.getName());
        return ResponseEntity.ok(
                PaginationUtils.toPagedResponse(ticketService.searchTicketsPagedForUser(q, currentUser, pageable)));
    }


}
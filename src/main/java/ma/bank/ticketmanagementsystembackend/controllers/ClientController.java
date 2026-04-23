package ma.bank.ticketmanagementsystembackend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.bank.ticketmanagementsystembackend.dtos.SuspendRequest;
import ma.bank.ticketmanagementsystembackend.dtos.dto.ClientDTO;
import ma.bank.ticketmanagementsystembackend.entities.Client;
import ma.bank.ticketmanagementsystembackend.entities.ClientStatus;
import ma.bank.ticketmanagementsystembackend.services.ClientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@CrossOrigin("*")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ClientDTO> createClient(@RequestBody Client client) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(clientService.createClient(client));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ClientDTO> updateClient(
            @PathVariable Long id,
            @RequestBody Client client) {
        return ResponseEntity.ok(clientService.updateClient(id, client));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<ClientDTO> getClient(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getClientById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<List<ClientDTO>> getAllClients() {
        return ResponseEntity.ok(clientService.getAllClients());
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<ClientDTO>> getClientsByStatus(@PathVariable ClientStatus status) {
        return ResponseEntity.ok(clientService.getClientsByStatus(status));
    }

    @PutMapping("/{id}/suspend")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ClientDTO> suspendClient(
            @PathVariable Long id,
            @RequestBody @Valid SuspendRequest request) {

        String adminEmail = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return ResponseEntity.ok(
                clientService.suspendClient(id, request.suspensionReason(), adminEmail));
    }

    @PutMapping("/{id}/reactivate")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ClientDTO> reactivateClient(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.reactivateClient(id));
    }
}
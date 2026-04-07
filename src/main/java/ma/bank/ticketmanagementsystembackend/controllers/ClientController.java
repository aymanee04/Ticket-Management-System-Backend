package ma.bank.ticketmanagementsystembackend.controllers;

import lombok.AllArgsConstructor;
import ma.bank.ticketmanagementsystembackend.dtos.UpdateClientRequest;
import ma.bank.ticketmanagementsystembackend.dtos.dto.ClientDTO;
import ma.bank.ticketmanagementsystembackend.entities.AppUser;
import ma.bank.ticketmanagementsystembackend.entities.Client;
import ma.bank.ticketmanagementsystembackend.entities.ClientStatus;
import ma.bank.ticketmanagementsystembackend.mappers.ClientMapper;
import ma.bank.ticketmanagementsystembackend.repositories.ClientRepository;
import ma.bank.ticketmanagementsystembackend.repositories.UserRepository;
import ma.bank.ticketmanagementsystembackend.services.ClientService;
import ma.bank.ticketmanagementsystembackend.services.EmailService;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clients")
@CrossOrigin("*")
@AllArgsConstructor
public class ClientController {
    private final ClientService clientService;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final ClientMapper clientMapper;


    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<ClientDTO> createClient(@RequestBody Client client) {
        return ResponseEntity.ok(clientService.createClient(client));
    }

    @PutMapping("/client/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<ClientDTO> updateClient(@PathVariable Long id, @RequestBody Client client) {
        client.setClientId(id);
        return ResponseEntity.ok(clientService.updateClient(client));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('USER')")
    public ResponseEntity<ClientDTO> getClient(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getClientById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('MANAGER','ADMIN')")
    public ResponseEntity<List<ClientDTO>> getAllClients() {
        return ResponseEntity.ok(clientService.getAllClients());
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<List<ClientDTO>> getClientsByStatus(@PathVariable ClientStatus status) {
        return ResponseEntity.ok(clientService.getClientsByStatus(status));
    }

    @PutMapping("/{id}/suspend")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ClientDTO> suspendClient(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        String reason = request.get("suspensionReason");

        if (reason == null || reason.trim().isEmpty()) {
            throw new RuntimeException("Suspension reason is required");
        }

        if (reason.length() < 10) {
            throw new RuntimeException("Suspension reason must be at least 10 characters");
        }

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        if (client.getStatus() == ClientStatus.SUSPENDED) {
            throw new RuntimeException("Client is already suspended");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AppUser admin = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Admin user not found"));

        client.setStatus(ClientStatus.SUSPENDED);
        client.setSuspensionReason(reason);
        client.setSuspendedAt(LocalDateTime.now());
        client.setSuspendedBy(admin.getUserId());

        Client updated = clientRepository.save(client);

        return ResponseEntity.ok(clientMapper.toDTO(updated));
    }

    @PutMapping("/{id}/reactivate")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ClientDTO> reactivateClient(@PathVariable Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        client.setStatus(ClientStatus.ACTIVE);
        client.setSuspensionReason(null);
        client.setSuspendedAt(null);
        client.setSuspendedBy(null);

        Client updated = clientRepository.save(client);

        return ResponseEntity.ok(clientMapper.toDTO(updated));
    }
}


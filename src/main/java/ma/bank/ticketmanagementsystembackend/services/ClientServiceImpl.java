package ma.bank.ticketmanagementsystembackend.services;

import ma.bank.ticketmanagementsystembackend.exceptions.BusinessException;
import ma.bank.ticketmanagementsystembackend.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import ma.bank.ticketmanagementsystembackend.dtos.dto.ClientDTO;
import ma.bank.ticketmanagementsystembackend.entities.AppUser;
import ma.bank.ticketmanagementsystembackend.entities.Client;
import ma.bank.ticketmanagementsystembackend.entities.ClientStatus;

import ma.bank.ticketmanagementsystembackend.mappers.ClientMapper;
import ma.bank.ticketmanagementsystembackend.repositories.ClientRepository;
import ma.bank.ticketmanagementsystembackend.repositories.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final ClientMapper clientMapper;

    @Override
    @Transactional
    @CacheEvict(value = "clients", allEntries = true)
    public ClientDTO createClient(Client client) {
        client.setStatus(ClientStatus.ACTIVE);
        client.setCreatedAt(LocalDateTime.now());
        return clientMapper.toDTO(clientRepository.save(client));
    }

    @Override
    @Transactional
    @CacheEvict(value = "clients", allEntries = true)
    public ClientDTO updateClient(Long id, Client client) {
        Client updatedClient = findClientById(id);

        updatedClient.setName(client.getName());
        updatedClient.setEmail(client.getEmail());
        updatedClient.setPhone(client.getPhone());
        updatedClient.setCompany(client.getCompany());

        return clientMapper.toDTO(clientRepository.save(updatedClient));
    }

    @Override
    @Cacheable(value = "clients", key = "#id")
    public ClientDTO getClientById(Long id) {
        return clientMapper.toDTO(findClientById(id));
    }

    @Override
    @Cacheable(value = "clients", key = "'all'")
    public List<ClientDTO> getAllClients() {
        return clientRepository.findAll()
                .stream()
                .map(clientMapper::toDTO)
                .toList();
    }

    @Override
    public List<ClientDTO> getClientsByStatus(ClientStatus status) {
        return clientRepository.findByStatus(status)
                .stream()
                .map(clientMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(value = "clients", allEntries = true)
    public ClientDTO suspendClient(Long id, String reason, String adminEmail) {
        Client client = findClientById(id);

        if (client.getStatus() == ClientStatus.SUSPENDED) {
            throw new BusinessException("Client is already suspended");
        }

        AppUser admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found: " + adminEmail));

        client.setStatus(ClientStatus.SUSPENDED);
        client.setSuspensionReason(reason);
        client.setSuspendedAt(LocalDateTime.now());
        client.setSuspendedBy(admin.getUserId());

        return clientMapper.toDTO(clientRepository.save(client));
    }

    @Override
    @Transactional
    @CacheEvict(value = "clients", allEntries = true)
    public ClientDTO reactivateClient(Long id) {
        Client client = findClientById(id);

        if (client.getStatus() == ClientStatus.ACTIVE) {
            throw new BusinessException("Client is already active");
        }
        if (client.getStatus() != ClientStatus.SUSPENDED && client.getStatus() != ClientStatus.INACTIVE) {
            throw new BusinessException("Client can only be reactivated from SUSPENDED or INACTIVE status");
        }

        client.setStatus(ClientStatus.ACTIVE);
        client.setSuspensionReason(null);
        client.setSuspendedAt(null);
        client.setSuspendedBy(null);

        return clientMapper.toDTO(clientRepository.save(client));
    }


    private Client findClientById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
    }
}
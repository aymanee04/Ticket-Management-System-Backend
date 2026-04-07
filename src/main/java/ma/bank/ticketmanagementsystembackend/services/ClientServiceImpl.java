package ma.bank.ticketmanagementsystembackend.services;

import lombok.AllArgsConstructor;
import ma.bank.ticketmanagementsystembackend.dtos.dto.ClientDTO;
import ma.bank.ticketmanagementsystembackend.entities.Client;
import ma.bank.ticketmanagementsystembackend.entities.ClientStatus;
import ma.bank.ticketmanagementsystembackend.mappers.ClientMapper;
import ma.bank.ticketmanagementsystembackend.repositories.ClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ClientServiceImpl implements ClientService {
    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    @Override
    @Transactional
    public ClientDTO createClient(Client client) {
        client.setStatus(ClientStatus.ACTIVE);
        client.setCreatedAt(LocalDateTime.now());
        Client savedClient = clientRepository.save(client);
        return clientMapper.toDTO(savedClient);
    }

    @Override
    public ClientDTO updateClient(Client client) {
//        client.setStatus(ClientStatus.ACTIVE);
        client.setCreatedAt(LocalDateTime.now());
        Client savedClient = clientRepository.save(client);
        return clientMapper.toDTO(savedClient);
    }

    @Override
    public ClientDTO getClientById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client with id " + id + " not found"));
        return clientMapper.toDTO(client);
    }

    @Override
    public List<ClientDTO> getAllClients() {
        return clientRepository.findAll().stream()
                .map(clientMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClientDTO> getClientsByStatus(ClientStatus status) {
        return clientRepository.findByStatus(status).stream()
                .map(clientMapper::toDTO)
                .collect(Collectors.toList());
    }
}

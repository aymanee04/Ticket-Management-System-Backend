package ma.bank.ticketmanagementsystembackend.services;

import ma.bank.ticketmanagementsystembackend.dtos.dto.ClientDTO;
import ma.bank.ticketmanagementsystembackend.entities.Client;
import ma.bank.ticketmanagementsystembackend.entities.ClientStatus;
import ma.bank.ticketmanagementsystembackend.mappers.ClientMapper;
import ma.bank.ticketmanagementsystembackend.repositories.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceImplTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientMapper clientMapper;

    @InjectMocks
    private ClientServiceImpl clientService;

    private Client client;
    private ClientDTO clientDTO;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setClientId(1L);
        client.setName("BMCE");

        clientDTO = new ClientDTO();
        clientDTO.setClientId(1L);
        clientDTO.setName("BMCE");
    }

    @Test
    void createClient() {
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        when(clientMapper.toDTO(any(Client.class))).thenReturn(clientDTO);

        ClientDTO result = clientService.createClient(client);

        assertNotNull(result);
        assertEquals("BMCE", result.getName());

        assertEquals(ClientStatus.ACTIVE, client.getStatus());
        assertNotNull(client.getCreatedAt());

        verify(clientRepository).save(client);
        verify(clientMapper).toDTO(client);
    }

    @Test
    void updateClient() {
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        when(clientMapper.toDTO(any(Client.class))).thenReturn(clientDTO);

        ClientDTO result = clientService.updateClient(client);

        assertNotNull(result);
        assertEquals("BMCE", result.getName());

        assertNotNull(client.getCreatedAt());

        verify(clientRepository).save(client);
        verify(clientMapper).toDTO(client);

    }

    @Test
    void getClientById() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientMapper.toDTO(client)).thenReturn(clientDTO);

        ClientDTO result = clientService.getClientById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getClientId());

        verify(clientRepository).findById(1L);
        verify(clientMapper).toDTO(client);
    }

    @Test
    void throwExceptionWhenClientNotFound() {
        when(clientRepository.findById(2L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            clientService.getClientById(2L);
        });

        assertEquals("Client with id 2 not found", ex.getMessage());
    }

    @Test
    void getAllClients() {
        List<Client> clients = List.of(client);

        when(clientRepository.findAll()).thenReturn(clients);
        when(clientMapper.toDTO(client)).thenReturn(clientDTO);

        List<ClientDTO> result = clientService.getAllClients();

        assertEquals(1, result.size());
        assertEquals("BMCE", result.get(0).getName());

        verify(clientRepository).findAll();
        verify(clientMapper).toDTO(client);
    }

    @Test
    void getClientsByStatus() {
        List<Client> clients = List.of(client);

        when(clientRepository.findByStatus(ClientStatus.ACTIVE)).thenReturn(clients);
        when(clientMapper.toDTO(client)).thenReturn(clientDTO);

        List<ClientDTO> result = clientService.getClientsByStatus(ClientStatus.ACTIVE);

        assertEquals(1, result.size());

        verify(clientRepository).findByStatus(ClientStatus.ACTIVE);
        verify(clientMapper).toDTO(client);
    }
}
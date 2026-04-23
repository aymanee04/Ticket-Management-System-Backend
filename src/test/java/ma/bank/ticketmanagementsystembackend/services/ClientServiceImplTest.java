package ma.bank.ticketmanagementsystembackend.services;

import ma.bank.ticketmanagementsystembackend.dtos.dto.ClientDTO;
import ma.bank.ticketmanagementsystembackend.entities.AppUser;
import ma.bank.ticketmanagementsystembackend.entities.Client;
import ma.bank.ticketmanagementsystembackend.entities.ClientStatus;
import ma.bank.ticketmanagementsystembackend.exceptions.BusinessException;
import ma.bank.ticketmanagementsystembackend.exceptions.ResourceNotFoundException;
import ma.bank.ticketmanagementsystembackend.mappers.ClientMapper;
import ma.bank.ticketmanagementsystembackend.repositories.ClientRepository;
import ma.bank.ticketmanagementsystembackend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceImplTest {

    @Mock private ClientRepository clientRepository;
    @Mock private UserRepository userRepository;
    @Mock private ClientMapper clientMapper;

    @InjectMocks
    private ClientServiceImpl clientService;

    private Client client;
    private ClientDTO clientDTO;
    private AppUser admin;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setClientId(1L);
        client.setName("BMCE");

        clientDTO = new ClientDTO();
        clientDTO.setClientId(1L);
        clientDTO.setName("BMCE");

        admin = new AppUser();
        admin.setUserId(99L);
        admin.setEmail("admin@bank.ma");
    }


    @Nested
    @DisplayName("createClient")
    class CreateClient {

        @Test
        @DisplayName("saves client with ACTIVE status and createdAt set")
        void success() {
            when(clientRepository.save(any(Client.class))).thenReturn(client);
            when(clientMapper.toDTO(client)).thenReturn(clientDTO);

            ClientDTO result = clientService.createClient(client);

            assertNotNull(result);
            assertEquals("BMCE", result.getName());
            assertEquals(ClientStatus.ACTIVE, client.getStatus());
            assertNotNull(client.getCreatedAt());

            verify(clientRepository).save(client);
            verify(clientMapper).toDTO(client);
        }
    }


    @Nested
    @DisplayName("updateClient")
    class UpdateClient {

        @Test
        @DisplayName("updates existing client and returns DTO")
        void success() {
            when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
            when(clientRepository.save(any(Client.class))).thenReturn(client);
            when(clientMapper.toDTO(client)).thenReturn(clientDTO);

            ClientDTO result = clientService.updateClient(1L, client);

            assertNotNull(result);
            assertEquals("BMCE", result.getName());
            assertEquals(1L, client.getClientId());

            verify(clientRepository).findById(1L);
            verify(clientRepository).save(client);
            verify(clientMapper).toDTO(client);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when client does not exist")
        void clientNotFound() {
            when(clientRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> clientService.updateClient(99L, client));

            verify(clientRepository, never()).save(any());
        }
    }


    @Nested
    @DisplayName("getClientById")
    class GetClientById {

        @Test
        @DisplayName("returns DTO when client exists")
        void success() {
            when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
            when(clientMapper.toDTO(client)).thenReturn(clientDTO);

            ClientDTO result = clientService.getClientById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getClientId());

            verify(clientRepository).findById(1L);
            verify(clientMapper).toDTO(client);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException with correct message")
        void clientNotFound() {
            when(clientRepository.findById(2L)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> clientService.getClientById(2L));

            assertEquals("Client not found with id: 2", ex.getMessage());
        }
    }


    @Nested
    @DisplayName("getAllClients")
    class GetAllClients {

        @Test
        @DisplayName("returns all clients as DTOs")
        void success() {
            when(clientRepository.findAll()).thenReturn(List.of(client));
            when(clientMapper.toDTO(client)).thenReturn(clientDTO);

            List<ClientDTO> result = clientService.getAllClients();

            assertEquals(1, result.size());
            assertEquals("BMCE", result.get(0).getName());

            verify(clientRepository).findAll();
            verify(clientMapper).toDTO(client);
        }

        @Test
        @DisplayName("returns empty list when no clients exist")
        void empty() {
            when(clientRepository.findAll()).thenReturn(List.of());

            List<ClientDTO> result = clientService.getAllClients();

            assertTrue(result.isEmpty());
            verify(clientMapper, never()).toDTO(any());
        }
    }

    // ── getClientsByStatus ────────────────────────────────────────────────────

    @Nested
    @DisplayName("getClientsByStatus")
    class GetClientsByStatus {

        @Test
        @DisplayName("returns clients filtered by status")
        void success() {
            when(clientRepository.findByStatus(ClientStatus.ACTIVE)).thenReturn(List.of(client));
            when(clientMapper.toDTO(client)).thenReturn(clientDTO);

            List<ClientDTO> result = clientService.getClientsByStatus(ClientStatus.ACTIVE);

            assertEquals(1, result.size());

            verify(clientRepository).findByStatus(ClientStatus.ACTIVE);
            verify(clientMapper).toDTO(client);
        }
    }


    @Nested
    @DisplayName("suspendClient")
    class SuspendClient {

        @Test
        @DisplayName("suspends an active client and sets suspension fields")
        void success() {
            client.setStatus(ClientStatus.ACTIVE);

            ClientDTO suspendedDTO = new ClientDTO();
            suspendedDTO.setClientId(1L);

            when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
            when(userRepository.findByEmail("admin@bank.ma")).thenReturn(Optional.of(admin));
            when(clientRepository.save(client)).thenReturn(client);
            when(clientMapper.toDTO(client)).thenReturn(suspendedDTO);

            ClientDTO result = clientService.suspendClient(1L, "Suspicious activity on account", "admin@bank.ma");

            assertNotNull(result);
            assertEquals(ClientStatus.SUSPENDED, client.getStatus());
            assertEquals("Suspicious activity on account", client.getSuspensionReason());
            assertNotNull(client.getSuspendedAt());
            assertEquals(99L, client.getSuspendedBy());

            verify(clientRepository).save(client);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when client does not exist")
        void clientNotFound() {
            when(clientRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> clientService.suspendClient(99L, "Some reason here", "admin@bank.ma"));

            verify(clientRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws BusinessException when client is already suspended")
        void alreadySuspended() {
            client.setStatus(ClientStatus.SUSPENDED);
            when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> clientService.suspendClient(1L, "Some reason here", "admin@bank.ma"));

            assertEquals("Client is already suspended", ex.getMessage());
            verify(clientRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when admin email not found")
        void adminNotFound() {
            client.setStatus(ClientStatus.ACTIVE);
            when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
            when(userRepository.findByEmail("ghost@bank.ma")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> clientService.suspendClient(1L, "Some reason here", "ghost@bank.ma"));

            verify(clientRepository, never()).save(any());
        }
    }


    @Nested
    @DisplayName("reactivateClient")
    class ReactivateClient {

        @Test
        @DisplayName("reactivates a suspended client and clears suspension fields")
        void success() {
            client.setStatus(ClientStatus.SUSPENDED);
            client.setSuspensionReason("Some reason");
            client.setSuspendedBy(99L);

            ClientDTO reactivatedDTO = new ClientDTO();
            reactivatedDTO.setClientId(1L);

            when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
            when(clientRepository.save(client)).thenReturn(client);
            when(clientMapper.toDTO(client)).thenReturn(reactivatedDTO);

            ClientDTO result = clientService.reactivateClient(1L);

            assertNotNull(result);
            assertEquals(ClientStatus.ACTIVE, client.getStatus());
            assertNull(client.getSuspensionReason());
            assertNull(client.getSuspendedAt());
            assertNull(client.getSuspendedBy());

            verify(clientRepository).save(client);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when client does not exist")
        void clientNotFound() {
            when(clientRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> clientService.reactivateClient(99L));

            verify(clientRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws BusinessException when client is not suspended")
        void notSuspended() {
            client.setStatus(ClientStatus.ACTIVE);
            when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> clientService.reactivateClient(1L));

            assertEquals("Client is not currently suspended", ex.getMessage());
            verify(clientRepository, never()).save(any());
        }
    }
}
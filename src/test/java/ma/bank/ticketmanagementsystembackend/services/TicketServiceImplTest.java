package ma.bank.ticketmanagementsystembackend.services;

import ma.bank.ticketmanagementsystembackend.dtos.dto.TicketDTO;
import ma.bank.ticketmanagementsystembackend.entities.*;
import ma.bank.ticketmanagementsystembackend.exceptions.BusinessException;
import ma.bank.ticketmanagementsystembackend.exceptions.ResourceNotFoundException;
import ma.bank.ticketmanagementsystembackend.mappers.TicketMapper;
import ma.bank.ticketmanagementsystembackend.repositories.ClientRepository;
import ma.bank.ticketmanagementsystembackend.repositories.TicketRepository;
import ma.bank.ticketmanagementsystembackend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private TicketMapper ticketMapper;
    @Mock
    private EmailService emailService;
    @InjectMocks
    private TicketServiceImpl ticketService;
    private AppUser appUser;
    private Ticket ticket;
    private Ticket multiClientTicket;
    private TicketDTO ticketDTO;
    private Client client1;
    private Client client2;
    private Pageable pageable;

    @BeforeEach
    void setUp() {

        client1 = Client.builder()
                .clientId(1L)
                .name("RMA")
                .status(ClientStatus.ACTIVE)
                .build();

        client2 = Client.builder()
                .clientId(2L)
                .name("BMCE")
                .status(ClientStatus.ACTIVE)
                .build();

        appUser = AppUser.builder()
                .userId(1L)
                .client(client1)
                .build();

        ticket = Ticket.builder()
                .ticketId(1L)
                .title("ticket title")
                .description("ticket description")
                .clients(Set.of(client1))
                .createdBy(appUser)
                .build();

        multiClientTicket = Ticket.builder()
                .ticketId(2L)
                .title("ticket title")
                .description("ticket description")
                .clients(Set.of(client1, client2))
                .createdBy(appUser)
                .build();

        ticketDTO = new TicketDTO();
        ticketDTO.setTicketId(1L);
    }

    @Nested
    @DisplayName("Create ticket test")
    class CreateTicketTest {
    @Test
    @DisplayName("Should create ticket successfully")
    void shouldCreateTicket() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(appUser));
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client1));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        when(ticketMapper.toDTO(ticket)).thenReturn(ticketDTO);

        TicketDTO ticket1 = ticketService.createTicket("title",
                "description",
                1L,1L);

        assertNotNull(ticket1);
        assertEquals(1L, ticket1.getTicketId());

        verify(userRepository).findById(1L);
        verify(clientRepository).findById(1L);
        verify(ticketRepository).save(any(Ticket.class));
        verify(emailService).sendTicketCreatedEmail(ticket);
        verify(ticketMapper).toDTO(ticket);
    }

    @Test
    @DisplayName("Should throw Exception when creator not found")
    void shouldThrowCreatorNotFound(){
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ticketService.createTicket("title","desc",1L,1L);
        });

        assertEquals("Creator not found", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(ticketRepository,never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when client status isn't active")
    void shouldThrowExceptionClientNotActive(){
        appUser.getClient().setStatus(ClientStatus.SUSPENDED);
        when(userRepository.findById(1L)).thenReturn(Optional.of(appUser));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ticketService.createTicket("title", "desc", 1L, 1L);
        });

        assertEquals("Cannot create tickets. Company account is " +
                appUser.getClient().getStatus(),exception.getMessage());

        verify(ticketRepository, never()).save(any());
    }

    @DisplayName("Should skip when creator client is null")
    @Test
    void shouldSkipFirstIfWhenCreatorClientIsNull() {
        appUser.setClient(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(appUser));
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client1));
        when(ticketRepository.save(any())).thenReturn(ticket);
        when(ticketMapper.toDTO(any())).thenReturn(ticketDTO);

        TicketDTO result = ticketService.createTicket("title", "desc", 1L, 1L);

        assertNotNull(result);

        verify(ticketRepository).save(any());
    }

    @Test
    @DisplayName("Should throw exception when client not found")
    void shouldThrowExceptionClientNotFound(){
        when(userRepository.findById(1L)).thenReturn(Optional.of(appUser));
        when(clientRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ticketService.createTicket("title", "desc", 1L, 1L);
        });

        assertEquals("Client not found", exception.getMessage());
        verify(clientRepository).findById(1L);
        verify(ticketRepository,never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when client status not active")
    void shouldThrowExceptionWhenClientNotActive() {

        Client notActive = new Client();
        notActive.setClientId(1L);
        notActive.setStatus(ClientStatus.INACTIVE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(appUser));
        when(clientRepository.findById(1L)).thenReturn(Optional.of(notActive));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            ticketService.createTicket("title", "desc", 1L, 1L);
        });

        assertTrue(ex.getMessage().contains("Cannot create ticket for non active client"));
    }
    }

    @Nested
    @DisplayName("Create multi client ticket test")
    class CreateMultiClientsTicketTest {
    @Test
    @DisplayName("Should create multi client ticket successfully")
    void shouldCreateMultiClientTicket() {
        Set<Long> clientIds = Set.of(1L, 2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(appUser));
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client1));
        when(clientRepository.findById(2L)).thenReturn(Optional.of(client2));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(multiClientTicket);
        when(ticketMapper.toDTO(any(Ticket.class))).thenReturn(ticketDTO);

        TicketDTO result;
        result = ticketService.createMultiClientTicket(
                "title",
                "desc",
                1L,
                clientIds
        );

        assertNotNull(result);

        verify(emailService).sendIncidentNotification(multiClientTicket);
    }

    @Test
    @DisplayName("Should throw Exception when creator not found")
    void shouldThrowCreatorNotFound(){
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ticketService.createTicket("title","desc",1L,1L);
        });

        assertEquals("Creator not found", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(ticketRepository,never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when client id empty")
    void ShouldThrowWhenClientIdsEmpty(){
        when(userRepository.findById(1L)).thenReturn(Optional.of(appUser));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ticketService.createMultiClientTicket("title", "desc", 1L, Set.of());
        });

        assertEquals("At least one client must be specified", exception.getMessage());
        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when client id null")
    void ShouldThrowWhenClientIdsNull(){
        when(userRepository.findById(1L)).thenReturn(Optional.of(appUser));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ticketService.createMultiClientTicket("title", "desc", 1L, null);
        });

        assertEquals("At least one client must be specified", exception.getMessage());
        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when client status isn't active")
    void shouldThrowExceptionClientNotActive(){
        appUser.getClient().setStatus(ClientStatus.SUSPENDED);
        when(userRepository.findById(1L)).thenReturn(Optional.of(appUser));
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client1));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ticketService.createMultiClientTicket(
                    "title", "desc", 1L, Set.of(1L));
        });

        assertEquals("Cannot create ticket non active client " +
                client1.getName(),exception.getMessage());

        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when client not found")
    void shouldThrowExceptionClientNotFound(){
        when(userRepository.findById(1L)).thenReturn(Optional.of(appUser));
        when(clientRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ticketService.createTicket("title", "desc", 1L, 1L);
        });

        assertEquals("Client not found", exception.getMessage());
        verify(clientRepository).findById(1L);
        verify(ticketRepository,never()).save(any());
    }

    }

    @Nested
    @DisplayName("Assign ticket test")
    class AssignTicketTest {
        @Test
        @DisplayName("Should Assign ticket successfully")
        void shouldAssignTicketSuccessfully(){
            ticket.setStatus(TicketStatus.IN_PROGRESS);
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
            when(userRepository.findById(1L)).thenReturn(Optional.of(appUser));
            when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
            when(ticketMapper.toDTO(any(Ticket.class))).thenReturn(ticketDTO);

            TicketDTO result = ticketService.assignTicket(1L,1L);
            assertNotNull(result);

            ticket.setAssignedTo(appUser);
            ticket.setUpdatedAt(LocalDateTime.now());

            Ticket assignedTicket = ticketRepository.save(ticket);

            verify(ticketRepository).findById(1L);
            verify(emailService).sendTicketAssignedEmail(assignedTicket,appUser);
            verify(ticketMapper).toDTO(ticket);
        }

        @Test
        @DisplayName("Should throw ticket not found exception")
        void shouldThrowTicketNotFound(){
            when(ticketRepository.findById(1L)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                ticketService.assignTicket(
                        1L,1L);
            });

            assertEquals("Ticket not found", exception.getMessage());
            verify(ticketRepository).findById(1L);
            verify(userRepository,never()).findById(any());
        }

        @Test
        @DisplayName("Should throw Assignee not found exception")
        void shouldThrowAssigneeNotFound(){
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                ticketService.assignTicket(
                        1L,1L);
            });
            assertEquals("Assignee not found", exception.getMessage());

            verify(ticketRepository).findById(1L);
            verify(userRepository).findById(1L);
            verify(ticketRepository,never()).save(any());
        }
    }

    @Nested
    @DisplayName("Approve ticket test")
    class ApproveTicketTest {
        @Test
        @DisplayName("Should approve ticket successfully")
        void shouldApproveTicketSuccessfully(){
            ticket.setStatus(TicketStatus.IN_PROGRESS);
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
            when(userRepository.findById(1L)).thenReturn(Optional.of(appUser));
            when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
            when(ticketMapper.toDTO(any(Ticket.class))).thenReturn(ticketDTO);

            TicketDTO result = ticketService.approveTicket(1L,1L,"Approved");
            assertNotNull(result);

            assertEquals(TicketStatus.VALIDATED, ticket.getStatus());
            assertEquals(appUser, ticket.getValidatedBy());
            assertEquals("Approved", ticket.getValidationComment());
            assertNotNull(ticket.getValidatedAt());
            assertNotNull(ticket.getUpdatedAt());

            verify(ticketRepository).save(ticket);
            verify(emailService).sendTicketValidatedEmail(ticket);
            verify(ticketMapper).toDTO(ticket);
        }

        @Test
        @DisplayName("Should throw ticket not found exception")
        void shouldThrowTicketNotFound(){
            when(ticketRepository.findById(1L)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                ticketService.approveTicket(
                        1L,1L,"comment");
            });

            assertEquals("Ticket not found", exception.getMessage());
            verify(ticketRepository).findById(1L);
            verify(userRepository,never()).findById(any());
        }

        @Test
        @DisplayName("Should throw manager not found exception")
        void shouldThrowManagerNotFound(){
            ticket.setStatus(TicketStatus.IN_PROGRESS);
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                ticketService.approveTicket(
                        1L,1L,"comment");
            });

            assertEquals("Manager not found", exception.getMessage());

            verify(ticketRepository,never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when ticket status isn't in progress")
        void shouldThrowExceptionWhenTicketStatusNotInProgress(){
            ticket.setStatus(TicketStatus.VALIDATED);
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
            when(userRepository.findById(1L)).thenReturn(Optional.of(appUser));

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    ticketService.approveTicket(
                            1L, 1L, "comment"));

            assertEquals("Only IN_PROGRESS or PENDING_VALIDATION tickets can be approved",exception.getMessage());

            verify(ticketRepository, never()).save(any());
       };
       }

    @Nested
    @DisplayName("Reject ticket test")
    class RejectTicketTest {
        @Test
        @DisplayName("Should reject ticket successfully")
        void shouldRejectTicketSuccessfully(){
            ticket.setStatus(TicketStatus.IN_PROGRESS);
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
            when(userRepository.findById(1L)).thenReturn(Optional.of(appUser));
            when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
            when(ticketMapper.toDTO(any(Ticket.class))).thenReturn(ticketDTO);

            TicketDTO result = ticketService.rejectTicket(
                    1L,1L,"Rejected");
            assertNotNull(result);

            assertEquals(TicketStatus.REJECTED, ticket.getStatus());
            assertEquals(appUser, ticket.getValidatedBy());
            assertEquals("Rejected", ticket.getValidationComment());
            assertNotNull(ticket.getValidatedAt());
            assertNotNull(ticket.getUpdatedAt());

            verify(ticketRepository).save(ticket);
            verify(emailService).sendTicketRejectedEmail(ticket);
            verify(ticketMapper).toDTO(ticket);
        }

        @Test
        @DisplayName("Should throw ticket not found exception")
        void shouldThrowTicketNotFound(){
            when(ticketRepository.findById(1L)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                ticketService.rejectTicket(
                        1L,1L,"comment");
            });

            assertEquals("Ticket not found", exception.getMessage());
            verify(ticketRepository).findById(1L);
            verify(userRepository,never()).findById(any());
        }

        @Test
        @DisplayName("Should throw manager not found exception")
        void shouldThrowManagerNotFound(){
            ticket.setStatus(TicketStatus.IN_PROGRESS);
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                ticketService.rejectTicket(
                        1L,1L,"comment");
            });

            assertEquals("Manager not found", exception.getMessage());

            verify(ticketRepository,never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when ticket status isn't in progress")
        void shouldThrowExceptionWhenTicketStatusNotInProgress(){
            ticket.setStatus(TicketStatus.VALIDATED);
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
            when(userRepository.findById(1L)).thenReturn(Optional.of(appUser));

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    ticketService.rejectTicket(
                            1L, 1L, "comment"));

            assertEquals("Only IN_PROGRESS or PENDING_VALIDATION tickets can be rejected",exception.getMessage());

            verify(ticketRepository, never()).save(any());
        };
    }

    @Nested
    @DisplayName("Archive ticket test")
    class ArchiveTicketTest {
        @ParameterizedTest
        @DisplayName("Should archive ticket with valid statuses")
        @EnumSource(value = TicketStatus.class, names = {"VALIDATED", "REJECTED"})
        void shouldArchiveTicketWithValidStatuses(TicketStatus status){
            ticket.setStatus(status);
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
            when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
            when(ticketMapper.toDTO(any(Ticket.class))).thenReturn(ticketDTO);

            TicketDTO result = ticketService.archiveTicket(1L);
            assertNotNull(result);

            assertEquals(TicketStatus.ARCHIVED, ticket.getStatus());
            assertNotNull(ticket.getUpdatedAt());

            verify(ticketRepository).save(ticket);
            verify(ticketMapper).toDTO(ticket);
        }

        @Test
        @DisplayName("Should throw ticket not found exception")
        void shouldThrowTicketNotFound(){
            when(ticketRepository.findById(1L)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                ticketService.archiveTicket(
                        1L);
            });

            assertEquals("Ticket not found", exception.getMessage());
            verify(ticketRepository).findById(1L);
            verify(userRepository,never()).findById(any());
        }

        @Test
        @DisplayName("Should throw exception when ticket status isn't in validated or rejected")
        void shouldThrowExceptionWhenStatusInvalid(){
            ticket.setStatus(TicketStatus.IN_PROGRESS);

            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    ticketService.archiveTicket( 1L));

            assertEquals("Only VALIDATED or REJECTED tickets can be archived",exception.getMessage());

            verify(ticketRepository, never()).save(any());
        }

    }

    @Nested
    @DisplayName("Cancel ticket test")
    class CancelTicketTest {
        @Test
        @DisplayName("Should cancel ticket successfully")
        void shouldCancelTicketSuccessfully(){
            ticket.setStatus(TicketStatus.IN_PROGRESS);
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
            when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
            when(ticketMapper.toDTO(any(Ticket.class))).thenReturn(ticketDTO);

            TicketDTO result = ticketService.cancelTicket(
                    1L);
            assertNotNull(result);

            assertEquals(TicketStatus.CANCELLED, ticket.getStatus());
            assertNotNull(ticket.getUpdatedAt());

            verify(ticketRepository).save(ticket);
            verify(ticketMapper).toDTO(ticket);
        }

        @Test
        @DisplayName("Should throw ticket not found exception")
        void shouldThrowTicketNotFound(){
            when(ticketRepository.findById(1L)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                ticketService.cancelTicket(
                        1L);
            });

            assertEquals("Ticket not found", exception.getMessage());
            verify(ticketRepository).findById(1L);
            verify(userRepository,never()).findById(any());
        }

        @Test
        @DisplayName("Should throw exception when ticket status isn't in progress")
        void shouldThrowExceptionWhenTicketStatusNotInProgress(){
            ticket.setStatus(TicketStatus.VALIDATED);
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    ticketService.cancelTicket(
                            1L));

            assertEquals("Only IN_PROGRESS or PENDING_VALIDATION tickets can be cancelled",exception.getMessage());

            verify(ticketRepository, never()).save(any());
        };
    }

    @Nested
    @DisplayName("Get ticket by id test")
    class GetTicketByIdTest {
        @Test
        @DisplayName("Should get ticket by id successfully when role equals ADMIN")
        void shouldGetTicketByIdSuccessfullyWhenAdmin(){
            appUser.setRoles(Set.of(Role.ADMIN));
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
            when(ticketMapper.toDTO(any(Ticket.class))).thenReturn(ticketDTO);

            TicketDTO result = ticketService.getTicketById(1L,appUser);
            assertNotNull(result);
            assertEquals(ticketDTO.getTicketId(), result.getTicketId());

            verify(ticketRepository).findById(1L);
            verify(ticketMapper).toDTO(ticket);
        }

        @Test
        @DisplayName("Should get ticket by id successfully when role equals MANAGER and same client")
        void shouldGetTicketByIdSuccessfullyWhenManager(){
            appUser.setRoles(Set.of(Role.MANAGER));
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
            when(ticketMapper.toDTO(any(Ticket.class))).thenReturn(ticketDTO);

            TicketDTO result = ticketService.getTicketById(1L,appUser);
            assertNotNull(result);
            assertEquals(ticketDTO.getTicketId(), result.getTicketId());

            verify(ticketRepository).findById(1L);
            verify(ticketMapper).toDTO(ticket);
        }

        @Test
        @DisplayName("Should get ticket by id successfully when role equals USER")
        void shouldGetTicketByIdSuccessfullyWhenUser(){
            appUser.setRoles(Set.of(Role.USER));
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
            when(ticketMapper.toDTO(any(Ticket.class))).thenReturn(ticketDTO);

            TicketDTO result = ticketService.getTicketById(1L,appUser);
            assertNotNull(result);
            assertEquals(ticketDTO.getTicketId(), result.getTicketId());

            verify(ticketRepository).findById(1L);
            verify(ticketMapper).toDTO(ticket);
        }

        @Test
        @DisplayName("Should throw ticket not found exception")
        void shouldThrowTicketNotFound(){
            when(ticketRepository.findById(1L)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(ResourceNotFoundException.class, () -> {
                ticketService.getTicketById(
                        1L,appUser);
            });

            assertEquals("Ticket not found", exception.getMessage());

            verify(ticketRepository).findById(1L);
            verify(ticketMapper,never()).toDTO(any());        }

        @Test
        @DisplayName("Should throw exception when not allowed")
        void shouldThrowNotAllowed(){
            AppUser other = new AppUser();
            other.setUserId(49L);
            other.setRoles(Set.of(Role.USER));
            other.setClient(client2);

            ticket.setCreatedBy(appUser);

            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                ticketService.getTicketById(
                        1L,other);
            });

            assertEquals("You are not allowed to access this ticket", exception.getMessage());

            verify(ticketRepository).findById(1L);
            verify(ticketMapper,never()).toDTO(any());        }
    }

    @Nested
    @DisplayName("Query methods test")
    class QueryMethodes{
        @Test
        @DisplayName("Should get tickets successfully")
        void getAllTickets(){
            List<Ticket> tickets = List.of(ticket);
            when(ticketRepository.findAll()).thenReturn(tickets);
            when(ticketMapper.toDTO(ticket)).thenReturn(ticketDTO);

            List<TicketDTO> result = ticketService.getAllTickets();
            assertNotNull(result);
            assertEquals(1, result.size());

            verify(ticketRepository).findAll();
            verify(ticketMapper).toDTO(ticket);
        }

        @Test
        @DisplayName("Should get tickets by status successfully")
        void getTicketsByStatus(){
            List<Ticket> tickets = List.of(ticket);
            when(ticketRepository.findByStatus(TicketStatus.IN_PROGRESS)).thenReturn(tickets);
            when(ticketMapper.toDTO(ticket)).thenReturn(ticketDTO);

            List<TicketDTO> result = ticketService.getTicketsByStatus(TicketStatus.IN_PROGRESS);
            assertNotNull(result);
            assertEquals(1, result.size());

            verify(ticketRepository).findByStatus(TicketStatus.IN_PROGRESS);
            verify(ticketMapper).toDTO(ticket);
        }

        @Test
        @DisplayName("Should get tickets by user id successfully")
        void getTicketByUserId(){
            List<Ticket> tickets = List.of(ticket);
            when(ticketRepository.findTicketByCreatedBy_UserId(1L)).thenReturn(tickets);
            when(ticketMapper.toDTO(ticket)).thenReturn(ticketDTO);

            List<TicketDTO> result = ticketService.getTicketsByUserId(1L);
            assertNotNull(result);
            assertEquals(1, result.size());

            verify(ticketRepository).findTicketByCreatedBy_UserId(1L);
            verify(ticketMapper).toDTO(ticket);
        }

        @Test
        @DisplayName("Should get tickets by client id successfully")
        void getTicketByClientId(){
            List<Ticket> tickets = List.of(ticket);
            when(ticketRepository.findByClients_ClientId(1L)).thenReturn(tickets);
            when(ticketMapper.toDTO(ticket)).thenReturn(ticketDTO);

            List<TicketDTO> result = ticketService.getTicketsByClient(1L);
            assertNotNull(result);
            assertEquals(1, result.size());

            verify(ticketRepository).findByClients_ClientId(1L);
            verify(ticketMapper).toDTO(ticket);
        }

        @Test
        @DisplayName("Should get tickets by assignedTo successfully")
        void getTicketsByAssignedTo(){
            List<Ticket> tickets = List.of(ticket);
            when(ticketRepository.findByAssignedTo(appUser)).thenReturn(tickets);
            when(userRepository.findById(1L)).thenReturn(Optional.of(appUser));
            when(ticketMapper.toDTO(ticket)).thenReturn(ticketDTO);

            List<TicketDTO> result = ticketService.getTicketsAssignedTo(1L);
            assertNotNull(result);
            assertEquals(1, result.size());

            verify(ticketRepository).findByAssignedTo(appUser);
            verify(ticketMapper).toDTO(ticket);
        }

        @Test
        @DisplayName("Should throw exception when user not found in getTicketsByAssignedTo")
        void throwUserNotFoundException(){
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> ticketService.getTicketsAssignedTo(1L));

            assertEquals("User not found", exception.getMessage());

            verify(userRepository).findById(1L);
            verify(ticketRepository, never()).findByAssignedTo(any());
        }

        @Test
        @DisplayName("Should search tickets successfully")
        void searchTickets(){
            List<Ticket> tickets = List.of(ticket);
            when(ticketRepository.searchTickets("search")).thenReturn(tickets);
            when(ticketMapper.toDTO(ticket)).thenReturn(ticketDTO);

            List<TicketDTO> result = ticketService.searchTickets("search");
            assertNotNull(result);
            assertEquals(1, result.size());

            verify(ticketRepository).searchTickets("search");
            verify(ticketMapper).toDTO(ticket);
        }
    }

    @Nested
    @DisplayName("Query methods pagination test")
    class QueryMethodesWithPagination{
        @Test
        @DisplayName("Should get tickets successfully")
        void getAllTicketsWithPagination(){
            Page<Ticket> tickets = new PageImpl<>(List.of(ticket));
            when(ticketRepository.findAll(pageable)).thenReturn(tickets);
            when(ticketMapper.toDTO(ticket)).thenReturn(ticketDTO);

            Page<TicketDTO> result = ticketService.getAllTicketsWithPagination(pageable);
            assertNotNull(result);
            assertEquals(1, result.getContent().size());

            verify(ticketRepository).findAll(pageable);
            verify(ticketMapper).toDTO(ticket);
        }

        @Test
        @DisplayName("Should get tickets by status successfully")
        void getTicketsByStatusWithPagination(){
            Page<Ticket> tickets = new PageImpl<>(List.of(ticket));
            when(ticketRepository.findByStatus(TicketStatus.IN_PROGRESS,pageable)).thenReturn(tickets);
            when(ticketMapper.toDTO(ticket)).thenReturn(ticketDTO);

            Page<TicketDTO> result = ticketService.getTicketsByStatusWithPagination(TicketStatus.IN_PROGRESS,pageable);
            assertNotNull(result);
            assertEquals(1, result.getContent().size());

            verify(ticketRepository).findByStatus(TicketStatus.IN_PROGRESS,pageable);
            verify(ticketMapper).toDTO(ticket);
        }

        @Test
        @DisplayName("Should get tickets by status and client successfully")
        void getTicketsByStatusAndClientWithPagination(){
            Page<Ticket> tickets = new PageImpl<>(List.of(ticket));
            when(ticketRepository.findByStatusAndClients_ClientId(TicketStatus.IN_PROGRESS,1L,pageable)).thenReturn(tickets);
            when(ticketMapper.toDTO(ticket)).thenReturn(ticketDTO);

            Page<TicketDTO> result = ticketService.getTicketsByStatusAndClient(TicketStatus.IN_PROGRESS,1L,pageable);
            assertNotNull(result);
            assertEquals(1, result.getContent().size());

            verify(ticketRepository).findByStatusAndClients_ClientId(TicketStatus.IN_PROGRESS,1L,pageable);
            verify(ticketMapper).toDTO(ticket);
        }

        @Test
        @DisplayName("Should get tickets by client id successfully")
        void getTicketByClientIdWithPagination(){
            Page<Ticket> tickets = new PageImpl<>(List.of(ticket));
            when(ticketRepository.findByClients_ClientId(1L,pageable)).thenReturn(tickets);
            when(ticketMapper.toDTO(ticket)).thenReturn(ticketDTO);

            Page<TicketDTO> result = ticketService.getTicketsByClientId(1L,pageable);
            assertNotNull(result);
            assertEquals(1, result.getContent().size());

            verify(ticketRepository).findByClients_ClientId(1L,pageable);
            verify(ticketMapper).toDTO(ticket);
        }

        @Test
        @DisplayName("Should search tickets successfully")
        void searchTickets(){
            Page<Ticket> tickets = new PageImpl<>(List.of(ticket));
            when(ticketRepository.searchTickets("search",pageable)).thenReturn(tickets);
            when(ticketMapper.toDTO(ticket)).thenReturn(ticketDTO);

            Page<TicketDTO> result = ticketService.searchTicketsWithPagination("search",pageable);
            assertNotNull(result);
            assertEquals(1, result.getContent().size());

            verify(ticketRepository).searchTickets("search",pageable);
            verify(ticketMapper).toDTO(ticket);
        }

        @Test
        @DisplayName("Should search tickets by client successfully")
        void searchTicketsByClientWithPagination(){
            Page<Ticket> tickets = new PageImpl<>(List.of(ticket));
            when(ticketRepository.searchTicketsByClient("search",1L,pageable)).thenReturn(tickets);
            when(ticketMapper.toDTO(ticket)).thenReturn(ticketDTO);

            Page<TicketDTO> result = ticketService.searchTicketsByClient("search",1L,pageable);
            assertNotNull(result);
            assertEquals(1, result.getContent().size());

            verify(ticketRepository).searchTicketsByClient("search",1L,pageable);
            verify(ticketMapper).toDTO(ticket);
        }
    }

    @Nested
    @DisplayName("Query filtered methods test")
    class QueryFilteredMethodes{
        @Test
        @DisplayName("Should getAllTicketsWithPagination when isAdminOrManager equals true successfully")
        void getTicketsPagedForUser(){
            appUser.setRoles(Set.of(Role.ADMIN));
            Page<Ticket> page = new PageImpl<>(List.of(ticket));
            Pageable pageable = PageRequest.of(0, 10);

            when(ticketRepository.findAll(pageable)).thenReturn(page);
            when(ticketMapper.toDTO(ticket)).thenReturn(new TicketDTO());

            Page<TicketDTO> result =
                    ticketService.getTicketsPagedForUser(appUser, pageable);

            assertEquals(1, result.getContent().size());
            verify(ticketRepository).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("Should getTicketsPagedForUser when tickets client equals the user client")
        void getTicketsPagedForUser_client_shouldReturnClientTickets() {
            appUser.setRoles(Set.of(Role.USER));

            Page<Ticket> page = new PageImpl<>(List.of(ticket));

            when(ticketRepository.findByClients_ClientId(1L, pageable))
                    .thenReturn(page);
            when(ticketMapper.toDTO(ticket)).thenReturn(new TicketDTO());

            Page<TicketDTO> result =
                    ticketService.getTicketsPagedForUser(
                            appUser, pageable);

            assertEquals(1, result.getContent().size());
            verify(ticketRepository).findByClients_ClientId(1L, pageable);
        }

        @Test
        @DisplayName("should return empty page when user don't belong to the ticket client")
        void getTicketsPagedForUser_noAccess_shouldReturnEmpty() {

        }

        @Test
        @DisplayName("Should getTicketsByStatusWithPagination when isAdminOrManager equals true successfully")
        void getTicketsByStatusPagedForNonUser(){
            appUser.setRoles(Set.of(Role.MANAGER));
            Page<Ticket> page = new PageImpl<>(List.of(ticket));
            Pageable pageable = PageRequest.of(0, 10);

            when(ticketRepository.findByStatus(TicketStatus.IN_PROGRESS, pageable)).thenReturn(page);
            when(ticketMapper.toDTO(any())).thenReturn(new TicketDTO());

            Page<TicketDTO> result =
                    ticketService.getTicketsByStatusPagedForUser(
                            TicketStatus.IN_PROGRESS, appUser, pageable);

            assertEquals(1, result.getContent().size());
        }

        @Test
        @DisplayName("Should getTicketsByStatusAndClient when tickets client equals the user client")
        void getTicketsByStatusPagedForUser(){
            appUser.setRoles(Set.of(Role.USER));
            Page<Ticket> page = new PageImpl<>(List.of(ticket));
            Pageable pageable = PageRequest.of(0, 10);

            when(ticketRepository.findByStatusAndClients_ClientId(TicketStatus.IN_PROGRESS, 1L, pageable)).thenReturn(page);
            when(ticketMapper.toDTO(any())).thenReturn(new TicketDTO());

            Page<TicketDTO> result =
                    ticketService.getTicketsByStatusPagedForUser(
                            TicketStatus.IN_PROGRESS, appUser, pageable);

            assertEquals(1, result.getContent().size());
        }
    }

}
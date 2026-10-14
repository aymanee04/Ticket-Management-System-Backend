package ma.bank.ticketmanagementsystembackend.controllers;

import ma.bank.ticketmanagementsystembackend.dtos.*;
import ma.bank.ticketmanagementsystembackend.dtos.dto.TicketDTO;
import ma.bank.ticketmanagementsystembackend.entities.AppUser;
import ma.bank.ticketmanagementsystembackend.entities.Client;
import ma.bank.ticketmanagementsystembackend.entities.TicketStatus;
import ma.bank.ticketmanagementsystembackend.exceptions.BusinessException;
import ma.bank.ticketmanagementsystembackend.services.TicketService;
import ma.bank.ticketmanagementsystembackend.services.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Ticket Controller Unit Tests")
class TicketControllerTest {

    @Mock
    private TicketService ticketService;

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @Mock
    private AppUser currentUser;

    @Mock
    private Client client;

    @InjectMocks
    private TicketController ticketController;

    private TicketDTO ticketDTO() {
        return TicketDTO.builder()
                .ticketId(1L)
                .title("Test Ticket")
                .description("Test description")
                .build();
    }

    @Test
    @DisplayName("Should create ticket successfully")
    void shouldCreateTicketSuccessfully() {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setTitle("Test Ticket");
        request.setDescription("Test description");

        when(authentication.getName())
                .thenReturn("user@test.com");

        when(userService.loadUserByEmail("user@test.com"))
                .thenReturn(currentUser);

        when(currentUser.getClient())
                .thenReturn(client);

        when(currentUser.getUserId())
                .thenReturn(10L);

        when(client.getClientId())
                .thenReturn(20L);

        TicketDTO ticket = ticketDTO();

        when(ticketService.createTicket(
                "Test Ticket",
                "Test description",
                10L,
                20L
        )).thenReturn(ticket);

        ResponseEntity<TicketDTO> response =
                ticketController.createTicket(authentication, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(ticket, response.getBody());

        verify(ticketService).createTicket(
                "Test Ticket",
                "Test description",
                10L,
                20L
        );
    }

    @Test
    @DisplayName("Should reject ticket creation when user has no client")
    void shouldRejectTicketCreationWhenUserHasNoClient() {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setTitle("Test Ticket");
        request.setDescription("Description");

        when(authentication.getName())
                .thenReturn("user@test.com");

        when(userService.loadUserByEmail("user@test.com"))
                .thenReturn(currentUser);

        when(currentUser.getClient())
                .thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> ticketController.createTicket(
                        authentication,
                        request
                )
        );

        assertEquals(
                "User must belong to a client to create tickets",
                exception.getMessage()
        );

        verify(ticketService, never())
                .createTicket(any(), any(), anyLong(), anyLong());
    }

    @Test
    @DisplayName("Should create incident successfully")
    void shouldCreateIncidentSuccessfully() {
        CreateIncidentRequest request = new CreateIncidentRequest();
        request.setTitle("Server Incident");
        request.setDescription("Server is unavailable");
        request.setClientIds(Set.of(1L, 2L));

        when(authentication.getName())
                .thenReturn("admin@test.com");

        when(userService.loadUserByEmail("admin@test.com"))
                .thenReturn(currentUser);

        when(currentUser.getUserId())
                .thenReturn(100L);

        TicketDTO ticket = ticketDTO();

        when(ticketService.createMultiClientTicket(
                "Server Incident",
                "Server is unavailable",
                100L,
                Set.of(1L, 2L)
        )).thenReturn(ticket);

        ResponseEntity<TicketDTO> response =
                ticketController.createIncident(
                        authentication,
                        request
                );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(ticket, response.getBody());

        verify(ticketService).createMultiClientTicket(
                "Server Incident",
                "Server is unavailable",
                100L,
                Set.of(1L, 2L)
        );
    }

    @Test
    @DisplayName("Should assign ticket successfully")
    void shouldAssignTicketSuccessfully() {
        Long ticketId = 1L;

        AssignTicketRequest request = new AssignTicketRequest();
        request.setAssignedToId(10L);

        TicketDTO ticket = ticketDTO();

        when(ticketService.assignTicket(ticketId, 10L))
                .thenReturn(ticket);

        ResponseEntity<TicketDTO> response =
                ticketController.assignTicket(ticketId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ticket, response.getBody());

        verify(ticketService)
                .assignTicket(ticketId, 10L);
    }

    @Test
    @DisplayName("Should approve ticket successfully")
    void shouldApproveTicketSuccessfully() {
        Long ticketId = 1L;

        TicketActionRequest request = new TicketActionRequest();
        request.setComment("Approved");

        when(authentication.getName())
                .thenReturn("manager@test.com");

        when(userService.loadUserByEmail("manager@test.com"))
                .thenReturn(currentUser);

        when(currentUser.getUserId())
                .thenReturn(50L);

        TicketDTO ticket = ticketDTO();

        when(ticketService.approveTicket(
                ticketId,
                50L,
                "Approved"
        )).thenReturn(ticket);

        ResponseEntity<TicketDTO> response =
                ticketController.approveTicket(
                        authentication,
                        ticketId,
                        request
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ticket, response.getBody());

        verify(ticketService)
                .approveTicket(ticketId, 50L, "Approved");
    }

    @Test
    @DisplayName("Should reject ticket successfully")
    void shouldRejectTicketSuccessfully() {
        Long ticketId = 1L;

        TicketActionRequest request = new TicketActionRequest();
        request.setComment("Rejected because information is incomplete");

        when(authentication.getName())
                .thenReturn("manager@test.com");

        when(userService.loadUserByEmail("manager@test.com"))
                .thenReturn(currentUser);

        when(currentUser.getUserId())
                .thenReturn(50L);

        TicketDTO ticket = ticketDTO();

        when(ticketService.rejectTicket(
                ticketId,
                50L,
                request.getComment()
        )).thenReturn(ticket);

        ResponseEntity<TicketDTO> response =
                ticketController.rejectTicket(
                        authentication,
                        ticketId,
                        request
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ticket, response.getBody());

        verify(ticketService)
                .rejectTicket(ticketId, 50L, request.getComment());
    }

    @Test
    @DisplayName("Should archive ticket successfully")
    void shouldArchiveTicketSuccessfully() {
        TicketDTO ticket = ticketDTO();

        when(ticketService.archiveTicket(1L))
                .thenReturn(ticket);

        ResponseEntity<TicketDTO> response =
                ticketController.archiveTicket(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ticket, response.getBody());

        verify(ticketService).archiveTicket(1L);
    }

    @Test
    @DisplayName("Should cancel ticket successfully")
    void shouldCancelTicketSuccessfully() {
        TicketDTO ticket = ticketDTO();

        when(ticketService.cancelTicket(1L))
                .thenReturn(ticket);

        ResponseEntity<TicketDTO> response =
                ticketController.cancelTicket(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ticket, response.getBody());

        verify(ticketService).cancelTicket(1L);
    }

    @Test
    @DisplayName("Should hard delete ticket successfully")
    void shouldHardDeleteTicketSuccessfully() {
        doNothing()
                .when(ticketService)
                .hardDeleteTicket(1L);

        ResponseEntity<Void> response =
                ticketController.hardDeleteTicket(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(ticketService).hardDeleteTicket(1L);
    }

    @Test
    @DisplayName("Should get ticket successfully")
    void shouldGetTicketSuccessfully() {
        when(authentication.getName())
                .thenReturn("user@test.com");

        when(userService.loadUserByEmail("user@test.com"))
                .thenReturn(currentUser);

        TicketDTO ticket = ticketDTO();

        when(ticketService.getTicketById(1L, currentUser))
                .thenReturn(ticket);

        ResponseEntity<TicketDTO> response =
                ticketController.getTicket(1L, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ticket, response.getBody());

        verify(ticketService)
                .getTicketById(1L, currentUser);
    }

    @Test
    @DisplayName("Should get all tickets successfully")
    void shouldGetAllTicketsSuccessfully() {
        List<TicketDTO> tickets = List.of(ticketDTO());

        when(ticketService.getAllTickets())
                .thenReturn(tickets);

        ResponseEntity<List<TicketDTO>> response =
                ticketController.getAllTickets();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tickets, response.getBody());

        verify(ticketService).getAllTickets();
    }

    @Test
    @DisplayName("Should get tickets by status successfully")
    void shouldGetTicketsByStatusSuccessfully() {
        TicketStatus status = TicketStatus.IN_PROGRESS;

        List<TicketDTO> tickets = List.of(ticketDTO());

        when(ticketService.getTicketsByStatus(status))
                .thenReturn(tickets);

        ResponseEntity<List<TicketDTO>> response =
                ticketController.getTicketsByStatus(status);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tickets, response.getBody());

        verify(ticketService).getTicketsByStatus(status);
    }

    @Test
    @DisplayName("Should get tickets by user successfully")
    void shouldGetTicketsByUserSuccessfully() {
        List<TicketDTO> tickets = List.of(ticketDTO());

        when(ticketService.getTicketsByUserId(10L))
                .thenReturn(tickets);

        ResponseEntity<List<TicketDTO>> response =
                ticketController.getTicketsByUserId(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tickets, response.getBody());

        verify(ticketService).getTicketsByUserId(10L);
    }

    @Test
    @DisplayName("Should get tickets by client successfully")
    void shouldGetTicketsByClientSuccessfully() {
        List<TicketDTO> tickets = List.of(ticketDTO());

        when(ticketService.getTicketsByClient(20L))
                .thenReturn(tickets);

        ResponseEntity<List<TicketDTO>> response =
                ticketController.getTicketsByClient(20L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tickets, response.getBody());

        verify(ticketService).getTicketsByClient(20L);
    }

    @Test
    @DisplayName("Should get tickets assigned to user successfully")
    void shouldGetTicketsAssignedToUserSuccessfully() {
        List<TicketDTO> tickets = List.of(ticketDTO());

        when(ticketService.getTicketsAssignedTo(10L))
                .thenReturn(tickets);

        ResponseEntity<List<TicketDTO>> response =
                ticketController.getTicketsAssignedTo(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tickets, response.getBody());

        verify(ticketService).getTicketsAssignedTo(10L);
    }

    @Test
    @DisplayName("Should search tickets successfully")
    void shouldSearchTicketsSuccessfully() {
        List<TicketDTO> tickets = List.of(ticketDTO());

        when(ticketService.searchTickets("database"))
                .thenReturn(tickets);

        ResponseEntity<List<TicketDTO>> response =
                ticketController.searchTickets("database");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tickets, response.getBody());

        verify(ticketService).searchTickets("database");
    }

    @Test
    @DisplayName("Should get paginated tickets successfully")
    void shouldGetPaginatedTicketsSuccessfully() {
        when(authentication.getName())
                .thenReturn("user@test.com");

        when(userService.loadUserByEmail("user@test.com"))
                .thenReturn(currentUser);

        TicketDTO ticket = ticketDTO();

        Page<TicketDTO> page = new PageImpl<>(
                List.of(ticket),
                PageRequest.of(0, 10),
                1
        );

        when(ticketService.getTicketsPagedForUser(
                eq(currentUser),
                any()
        )).thenReturn(page);

        ResponseEntity<PagedResponse<TicketDTO>> response =
                ticketController.getAllTicketsPaged(
                        authentication,
                        0,
                        10,
                        "createdAt",
                        "desc"
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        assertEquals(
                List.of(ticket),
                response.getBody().getContent()
        );

        assertEquals(0, response.getBody().getPageNumber());
        assertEquals(10, response.getBody().getPageSize());
        assertEquals(1, response.getBody().getTotalElements());

        verify(ticketService)
                .getTicketsPagedForUser(
                        eq(currentUser),
                        any()
                );
    }

    @Test
    @DisplayName("Should get paginated tickets by status successfully")
    void shouldGetPaginatedTicketsByStatusSuccessfully() {
        TicketStatus status = TicketStatus.IN_PROGRESS;

        when(authentication.getName())
                .thenReturn("user@test.com");

        when(userService.loadUserByEmail("user@test.com"))
                .thenReturn(currentUser);

        TicketDTO ticket = ticketDTO();

        Page<TicketDTO> page = new PageImpl<>(
                List.of(ticket),
                PageRequest.of(0, 10),
                1
        );

        when(ticketService.getTicketsByStatusPagedForUser(
                eq(status),
                eq(currentUser),
                any()
        )).thenReturn(page);

        ResponseEntity<PagedResponse<TicketDTO>> response =
                ticketController.getTicketsByStatusPaged(
                        authentication,
                        status,
                        0,
                        10
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(List.of(ticket), response.getBody().getContent());

        verify(ticketService)
                .getTicketsByStatusPagedForUser(
                        eq(status),
                        eq(currentUser),
                        any()
                );
    }

    @Test
    @DisplayName("Should search tickets with pagination successfully")
    void shouldSearchTicketsWithPaginationSuccessfully() {
        when(authentication.getName())
                .thenReturn("user@test.com");

        when(userService.loadUserByEmail("user@test.com"))
                .thenReturn(currentUser);

        TicketDTO ticket = ticketDTO();

        Page<TicketDTO> page = new PageImpl<>(
                List.of(ticket),
                PageRequest.of(0, 10),
                1
        );

        when(ticketService.searchTicketsPagedForUser(
                eq("database"),
                eq(currentUser),
                any()
        )).thenReturn(page);

        ResponseEntity<PagedResponse<TicketDTO>> response =
                ticketController.searchTicketsPaged(
                        authentication,
                        "database",
                        0,
                        10
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(List.of(ticket), response.getBody().getContent());

        verify(ticketService)
                .searchTicketsPagedForUser(
                        eq("database"),
                        eq(currentUser),
                        any()
                );
    }
}
package ma.bank.ticketmanagementsystembackend.controllers;

import ma.bank.ticketmanagementsystembackend.TestSecurityConfig;
import ma.bank.ticketmanagementsystembackend.dtos.dto.ClientDTO;
import ma.bank.ticketmanagementsystembackend.entities.Client;
import ma.bank.ticketmanagementsystembackend.services.ClientService;
import ma.bank.ticketmanagementsystembackend.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClientController.class)
@Import(TestSecurityConfig.class)
class ClientControllerTest {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private ClientService clientService;

    @MockitoBean
    private UserService userService;

    private MockMvc mockMvc;
    private ClientDTO clientDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        clientDTO = new ClientDTO();
        clientDTO.setClientId(1L);
        clientDTO.setName("RMA");
    }

    @Nested
    @DisplayName("POST /api/clients")
    class CreateClientTest {

        @Test
        @WithMockUser(authorities = "ADMIN")
        @DisplayName("ADMIN → 201 Created")
        void createClient() throws Exception {
            when(clientService.createClient(any(Client.class))).thenReturn(clientDTO);

            mockMvc.perform(post("/api/clients")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                { "name": "RMA" }
                                """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.clientId").value(1L))
                    .andExpect(jsonPath("$.name").value("RMA"));

            verify(clientService).createClient(any(Client.class));
        }

        @Test
        @WithMockUser(authorities = "USER")
        @DisplayName("USER → 403 Forbidden")
        void createClientForbidden() throws Exception {
            mockMvc.perform(post("/api/clients")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                { "name": "RMA" }
                                """))
                    .andExpect(status().isForbidden());

            verify(clientService, never()).createClient(any());
        }

        @Test
        @DisplayName("Unauthenticated → 401 Unauthorized")
        void createClientUnauthorized() throws Exception {
            mockMvc.perform(post("/api/clients")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                { "name": "RMA" }
                                """))
                    .andExpect(status().isUnauthorized());

            verify(clientService, never()).createClient(any());
        }
    }
}
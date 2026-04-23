package ma.bank.ticketmanagementsystembackend.services;

import ma.bank.ticketmanagementsystembackend.dtos.CreateUserRequest;
import ma.bank.ticketmanagementsystembackend.dtos.UpdateUserRequest;
import ma.bank.ticketmanagementsystembackend.dtos.dto.AppUserDTO;
import ma.bank.ticketmanagementsystembackend.entities.AppUser;
import ma.bank.ticketmanagementsystembackend.entities.Client;
import ma.bank.ticketmanagementsystembackend.entities.Role;
import ma.bank.ticketmanagementsystembackend.exceptions.BusinessException;
import ma.bank.ticketmanagementsystembackend.exceptions.DuplicateResourceException;
import ma.bank.ticketmanagementsystembackend.exceptions.InvalidPasswordException;
import ma.bank.ticketmanagementsystembackend.exceptions.ResourceNotFoundException;
import ma.bank.ticketmanagementsystembackend.mappers.AppUserMapper;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private AppUserMapper appUserMapper;
    @Mock private EmailService emailService;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private AppUser user;
    private AppUserDTO userDTO;
    private Client client;
    private CreateUserRequest createRequest;
    private UpdateUserRequest updateRequest;


    @BeforeEach
    void setUp() {
        client = new Client();
        client.setClientId(1L);
        client.setName("RMA");

        user = new AppUser();
        user.setUserId(1L);
        user.setName("Tester");
        user.setEmail("test@bank.ma");
        user.setPassword("plain123");
        user.setClient(client);

        userDTO = new AppUserDTO();
        userDTO.setUserId(1L);
        userDTO.setEmail("test@bank.ma");

        createRequest = new CreateUserRequest();
        createRequest.setName("Tester");
        createRequest.setEmail("test@bank.ma");
        createRequest.setPassword("plain123");
        createRequest.setJobTitle("Developer");
        createRequest.setClientId(1L);

        updateRequest = new UpdateUserRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setEmail("updated@bank.ma");
        updateRequest.setJobTitle("Senior Developer");
    }

    //  createUser

    @Nested
    @DisplayName("createUser")
    class CreateUser {

        @Test
        @DisplayName("creates user, encodes password, sends welcome email, adds USER role")
        void success() {
            when(userRepository.existsByEmail("test@bank.ma")).thenReturn(false);
            when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
            when(passwordEncoder.encode("plain123")).thenReturn("encoded123");
            when(userRepository.save(any(AppUser.class))).thenReturn(user);
            when(appUserMapper.toDTO(user)).thenReturn(userDTO);

            AppUserDTO result = userService.createUser(createRequest);

            assertNotNull(result);
            verify(userRepository).existsByEmail("test@bank.ma");
            verify(clientRepository).findById(1L);
            // welcome email must be called before encoding (plain password still available)
            verify(emailService).sendWelcomeEmail(any(AppUser.class), eq("plain123"));
            verify(passwordEncoder).encode("plain123");
            verify(userRepository).save(any(AppUser.class));
        }

        @Test
        @DisplayName("throws DuplicateResourceException when email already exists")
        void emailAlreadyExists() {
            when(userRepository.existsByEmail("test@bank.ma")).thenReturn(true);

            DuplicateResourceException ex = assertThrows(DuplicateResourceException.class,
                    () -> userService.createUser(createRequest));

            assertEquals("Email already exists", ex.getMessage());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when client not found")
        void clientNotFound() {
            when(userRepository.existsByEmail("test@bank.ma")).thenReturn(false);
            when(clientRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> userService.createUser(createRequest));

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("creates user without client when clientId is null")
        void noClient() {
            createRequest.setClientId(null);

            when(userRepository.existsByEmail("test@bank.ma")).thenReturn(false);
            when(passwordEncoder.encode("plain123")).thenReturn("encoded123");
            when(userRepository.save(any(AppUser.class))).thenReturn(user);
            when(appUserMapper.toDTO(user)).thenReturn(userDTO);

            AppUserDTO result = userService.createUser(createRequest);

            assertNotNull(result);
            verify(clientRepository, never()).findById(any());
        }

        @Test
        @DisplayName("always adds USER role even when roles list is provided without it")
        void alwaysAddsUserRole() {
            createRequest.setRoles(List.of(Role.MANAGER));

            when(userRepository.existsByEmail("test@bank.ma")).thenReturn(false);
            when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
            when(passwordEncoder.encode(any())).thenReturn("encoded");
            when(userRepository.save(any(AppUser.class))).thenAnswer(inv -> {
                AppUser saved = inv.getArgument(0);
                assertTrue(saved.getRoles().contains(Role.USER),
                        "USER role must always be present");
                return user;
            });
            when(appUserMapper.toDTO(user)).thenReturn(userDTO);

            userService.createUser(createRequest);
        }
    }

    //  updateUser

    @Nested
    @DisplayName("updateUser")
    class UpdateUser {

        @Test
        @DisplayName("updates name, email and jobTitle")
        void success() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);
            when(appUserMapper.toDTO(user)).thenReturn(userDTO);

            AppUserDTO result = userService.updateUser(1L, updateRequest);

            assertNotNull(result);
            assertEquals("Updated Name", user.getName());
            assertEquals("updated@bank.ma", user.getEmail());
            assertEquals("Senior Developer", user.getJobTitle());

            verify(userRepository).findById(1L);
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when user does not exist")
        void userNotFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> userService.updateUser(99L, updateRequest));

            verify(userRepository, never()).save(any());
        }
    }

    //  getUserById

    @Nested
    @DisplayName("getUserById")
    class GetUserById {

        @Test
        @DisplayName("returns DTO when user exists")
        void success() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(appUserMapper.toDTO(user)).thenReturn(userDTO);

            AppUserDTO result = userService.getUserById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getUserId());
            verify(userRepository).findById(1L);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException with correct message")
        void notFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> userService.getUserById(99L));

            assertEquals("User not found", ex.getMessage());
        }
    }

    //  loadUserByEmail

    @Nested
    @DisplayName("loadUserByEmail")
    class LoadUserByEmail {

        @Test
        @DisplayName("returns AppUser when email exists")
        void success() {
            when(userRepository.findByEmail("test@bank.ma")).thenReturn(Optional.of(user));

            AppUser result = userService.loadUserByEmail("test@bank.ma");

            assertEquals("test@bank.ma", result.getEmail());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException with email in message")
        void notFound() {
            when(userRepository.findByEmail("ghost@bank.ma")).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> userService.loadUserByEmail("ghost@bank.ma"));

            assertTrue(ex.getMessage().contains("ghost@bank.ma"));
        }
    }

    //  getAllUsers

    @Nested
    @DisplayName("getAllUsers")
    class GetAllUsers {

        @Test
        @DisplayName("returns all users as DTOs")
        void success() {
            when(userRepository.findAll()).thenReturn(List.of(user));
            when(appUserMapper.toDTO(user)).thenReturn(userDTO);

            List<AppUserDTO> result = userService.getAllUsers();

            assertEquals(1, result.size());
            verify(userRepository).findAll();
        }

        @Test
        @DisplayName("returns empty list when no users exist")
        void empty() {
            when(userRepository.findAll()).thenReturn(List.of());

            List<AppUserDTO> result = userService.getAllUsers();

            assertTrue(result.isEmpty());
            verify(appUserMapper, never()).toDTO(any());
        }
    }

    //  changePassword

    @Nested
    @DisplayName("changePassword")
    class ChangePassword {

        @Test
        @DisplayName("encodes new password and sends notification email")
        void success() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("plain123", "plain123")).thenReturn(true);
            when(passwordEncoder.matches("newPass9", "plain123")).thenReturn(false);
            when(passwordEncoder.encode("newPass9")).thenReturn("encodedNew");

            userService.changePassword(1L, "plain123", "newPass9");

            assertEquals("encodedNew", user.getPassword());
            verify(userRepository).save(user);
            verify(emailService).sendPasswordChangedEmail(user);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when user not found")
        void userNotFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> userService.changePassword(99L, "any", "newPass9"));

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws InvalidPasswordException when current password is wrong")
        void wrongCurrentPassword() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrongPass", "plain123")).thenReturn(false);

            InvalidPasswordException ex = assertThrows(InvalidPasswordException.class,
                    () -> userService.changePassword(1L, "wrongPass", "newPass9"));

            assertEquals("Current password is incorrect", ex.getMessage());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws InvalidPasswordException when new password equals current")
        void samePassword() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("plain123", "plain123")).thenReturn(true);

            InvalidPasswordException ex = assertThrows(InvalidPasswordException.class,
                    () -> userService.changePassword(1L, "plain123", "plain123"));

            assertEquals("New password must be different from current password", ex.getMessage());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws InvalidPasswordException when new password is too short")
        void passwordTooShort() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("plain123", "plain123")).thenReturn(true);
            when(passwordEncoder.matches("abc", "plain123")).thenReturn(false);

            InvalidPasswordException ex = assertThrows(InvalidPasswordException.class,
                    () -> userService.changePassword(1L, "plain123", "abc"));

            assertEquals("New password must be at least 6 characters", ex.getMessage());
            verify(userRepository, never()).save(any());
        }
    }

    //  addRoleToUser

    @Nested
    @DisplayName("addRoleToUser")
    class AddRoleToUser {

        @Test
        @DisplayName("adds role when user exists and role is valid")
        void success() {
            when(userRepository.findByName("Tester")).thenReturn(user);

            userService.addRoleToUser("Tester", "MANAGER");

            assertTrue(user.getRoles().contains(Role.MANAGER));
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("does not save when user already has the role")
        void alreadyHasRole() {
            user.setRoles(new java.util.ArrayList<>(List.of(Role.MANAGER)));
            when(userRepository.findByName("Tester")).thenReturn(user);

            userService.addRoleToUser("Tester", "MANAGER");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when user not found")
        void userNotFound() {
            when(userRepository.findByName("Ghost")).thenReturn(null);

            assertThrows(ResourceNotFoundException.class,
                    () -> userService.addRoleToUser("Ghost", "MANAGER"));
        }

        @Test
        @DisplayName("throws BusinessException when role name is invalid")
        void invalidRole() {
            when(userRepository.findByName("Tester")).thenReturn(user);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> userService.addRoleToUser("Tester", "SUPERUSER"));

            assertTrue(ex.getMessage().contains("SUPERUSER"));
        }
    }
}
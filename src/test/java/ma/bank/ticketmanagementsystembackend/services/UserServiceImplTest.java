package ma.bank.ticketmanagementsystembackend.services;

import ma.bank.ticketmanagementsystembackend.dtos.UpdateUserRequest;
import ma.bank.ticketmanagementsystembackend.dtos.dto.AppUserDTO;
import ma.bank.ticketmanagementsystembackend.entities.AppUser;
import ma.bank.ticketmanagementsystembackend.entities.Client;
import ma.bank.ticketmanagementsystembackend.entities.Role;
import ma.bank.ticketmanagementsystembackend.mappers.AppUserMapper;
import ma.bank.ticketmanagementsystembackend.repositories.ClientRepository;
import ma.bank.ticketmanagementsystembackend.repositories.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AppUserMapper appUserMapper;
    @InjectMocks
    private UserServiceImpl userService;

    private AppUser user;
    private Client client;
    private AppUserDTO userDTO;
    private UpdateUserRequest updateUserRequest;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setClientId(1L);
        client.setName("RMA");

        user = new AppUser();
        user.setUserId(1L);
        user.setName("tester");
        user.setEmail("test@mail.com");
        user.setPassword("1234");
        user.setClient(client);

        updateUserRequest = new UpdateUserRequest();
        updateUserRequest.setName("QA");
        updateUserRequest.setEmail("test@gmail.com");
        updateUserRequest.setPassword("1234");
        updateUserRequest.setClientId(1L);

        userDTO = new AppUserDTO();
        userDTO.setUserId(1L);
        userDTO.setEmail("test@gmail.com");
        pageable = PageRequest.of(0, 10);
    }

    @Nested
    @DisplayName("Create User Test")
    class CreateUserTest{
        @Test
        @DisplayName("Should Create User Successfully")
        void shouldCreateUser(){
            when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
            when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(user.getPassword())).thenReturn("encodedPassword");
            when(userRepository.save(user)).thenReturn(user);
            when(appUserMapper.toDTO(user)).thenReturn(userDTO);

            AppUserDTO result = userService.createUserInternal(user);

            assertNotNull(result);
            assertEquals("test@gmail.com", result.getEmail());

            assertTrue(user.getRoles().contains(Role.USER));
            assertEquals("encodedPassword", user.getPassword());

            verify(clientRepository).findById(1L);
            verify(userRepository).existsByEmail("test@mail.com");
            verify(emailService).sendWelcomeEmail(user, "1234");
            verify(passwordEncoder).encode("1234");
            verify(userRepository,times(1)).save(user);
            verify(appUserMapper).toDTO(user);
        }

        @Test
        @DisplayName("Should Throw Exception when client not found")
        void shouldThrowClientNotFound(){
            when(clientRepository.findById(1L)).thenReturn(Optional.empty());

            RuntimeException exception= assertThrows(
                    RuntimeException.class,
                    () -> userService.createUserInternal(user)
            );
            assertEquals("Client not found",exception.getMessage());
            verify(clientRepository).findById(1L);
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should Throw Exception when email already exists")
        void shouldThrowEmailExistsException(){
            when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
            when(userRepository.existsByEmail("test@mail.com")).thenReturn(true);

            RuntimeException exception= assertThrows(
                    RuntimeException.class,
                    () -> userService.createUserInternal(user)
            );

            assertEquals("Email already exists", exception.getMessage());

            verify(userRepository).existsByEmail("test@mail.com");
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Update user test")
    class UpdateUserTest{
        @Test
        @DisplayName("Should update user successfully")
        void shouldUpdateUser(){
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.save(any(AppUser.class))).thenReturn(user);
            when(appUserMapper.toDTO(user)).thenReturn(userDTO);

            AppUserDTO updatedUser = userService.updateUser(1L, updateUserRequest);

            assertNotNull(updatedUser);

            assertEquals("QA",user.getName());

            verify(userRepository).findById(1L);
            verify(userRepository).save(user);
            verify(appUserMapper).toDTO(user);
        }

    }

    @Nested
    @DisplayName("Get user by id test")
    class GetUserById{
        @Test
        @DisplayName("Should get user by id")
        void shouldGetUserByIdSuccessfully(){
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(appUserMapper.toDTO(user)).thenReturn(userDTO);

            AppUserDTO result = userService.getUserById(1L);

            assertNotNull(result);
            assertEquals(userDTO.getUserId(), result.getUserId());

            verify(userRepository).findById(1L);
            verify(appUserMapper).toDTO(user);
        }

        @Test
        @DisplayName("Should throw user not found exception")
        void shouldThrowUserNotFoundException(){
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> userService.getUserById(1L)
            );

            assertEquals("User not found", exception.getMessage());

            verify(userRepository).findById(1L);
            verify(appUserMapper, never()).toDTO(any());
        }
    }

    @Nested
    @DisplayName("Query methods test")
    class QueryMethodes{
        @Test
        @DisplayName("Should get user by client name")
        void shouldGetUserByClientName(){
            List<AppUser> users = List.of(user);
            when(userRepository.findByClient_Name("RMA")).thenReturn(users);
            when(appUserMapper.toDTO(user)).thenReturn(userDTO);

            List<AppUserDTO> result = userService.getUserByClientName("RMA");

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(userDTO.getUserId(), result.get(0).getUserId());

            verify(userRepository).findByClient_Name("RMA");
            verify(appUserMapper).toDTO(user);
        }

        @Test
        @DisplayName("Should return an empty list when no user found with client name")
        void shouldReturnEmptyList(){
            when(userRepository.findByClient_Name("RMA")).thenReturn(List.of());

            List<AppUserDTO> result = userService.getUserByClientName("RMA");

            assertNotNull(result);
            assertTrue(result.isEmpty());

            verify(userRepository).findByClient_Name("RMA");
            verify(appUserMapper, never()).toDTO(any());
        }

        @Test
        @DisplayName("Should get user by client id")
        void shouldGetUserByClientId(){
            List<AppUser> users = List.of(user);
            when(userRepository.findByClient_ClientId(1L)).thenReturn(users);
            when(appUserMapper.toDTO(user)).thenReturn(userDTO);

            List<AppUserDTO> result = userService.getUserByClientId(1L);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(userDTO.getUserId(), result.get(0).getUserId());

            verify(userRepository).findByClient_ClientId(1L);
            verify(appUserMapper).toDTO(user);
        }

        @Test
        @DisplayName("Should return an empty list when no user found with client id")
        void shouldReturnEmptyListWhenNoUserFoundWithClientId(){
            when(userRepository.findByClient_ClientId(1L)).thenReturn(List.of());

            List<AppUserDTO> result = userService.getUserByClientId(1L);

            assertNotNull(result);
            assertTrue(result.isEmpty());

            verify(userRepository).findByClient_ClientId(1L);
            verify(appUserMapper, never()).toDTO(any());
        }

        @Test
        @DisplayName("Should get all users")
        void shouldGetAllUsers(){
            List<AppUser> users = List.of(user);
            when(userRepository.findAll()).thenReturn(users);
            when(appUserMapper.toDTO(user)).thenReturn(userDTO);

            List<AppUserDTO> result = userService.getAllUsers();

            assertNotNull(result);
            assertEquals(1, result.size());

            verify(userRepository).findAll();
            verify(appUserMapper).toDTO(user);
        }
    }

    @Nested
    @DisplayName("Load by email test")
    class LoadByEmail{
        @Test
        @DisplayName("Should Load User By Email")
        void shouldLoadUserByEmail(){
            when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(user));

            AppUser result = userService.loadUserByEmail("test@mail.com");

            assertEquals(user.getEmail(), result.getEmail());
        }

        @Test
        @DisplayName("Should throw user not found exception")
        void shouldThrowUserNotFoundByEmail(){
            when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> userService.loadUserByEmail("test@mail.com"));

            assertEquals("User not found with this email: " + user.getEmail(), exception.getMessage());
            verify(userRepository).findByEmail("test@mail.com");
            verify(appUserMapper, never()).toDTO(any());
        }
    }

    @Nested
    @DisplayName("Pagination methods test")
    class PaginationMethods{
        @Test
        @DisplayName("Should get all users with pagination")
        void shouldGetAllUsersWithPagination(){
            Page<AppUser> page = new PageImpl<>(List.of(user));
            when(userRepository.findAll(pageable)).thenReturn(page);
            when(appUserMapper.toDTO(user)).thenReturn(userDTO);

            Page<AppUserDTO> result = userService.getAllUsersWithPagination(pageable);

            assertNotNull(result);
            assertEquals(1,result.getContent().size());
            assertEquals(userDTO.getUserId(), result.getContent().get(0).getUserId());

            verify(userRepository).findAll(pageable);
            verify(appUserMapper).toDTO(user);
        }

        @Test
        @DisplayName("Should Get users by client id with pagination")
        void shouldGetUsersByClientIdWithPagination(){
            Page<AppUser> page = new PageImpl<>(List.of(user));
            when(userRepository.findByClient_ClientId(1L,pageable)).thenReturn(page);
            when(appUserMapper.toDTO(user)).thenReturn(userDTO);

            Page<AppUserDTO> result = userService.getUsersByClientIdWithPagination(1L,pageable);

            assertNotNull(result);
            assertEquals(1,result.getContent().size());
            assertEquals(userDTO.getUserId(), result.getContent().get(0).getUserId());

            verify(userRepository).findByClient_ClientId(1L,pageable);
            verify(appUserMapper).toDTO(user);
        }

        @Test
        @DisplayName("Should get user by id")
        void shouldGetUserByIdWithPagination(){
            Page<AppUser> page = new PageImpl<>(List.of(user));
            when(userRepository.findByUserId(1L,pageable)).thenReturn(page);
            when(appUserMapper.toDTO(user)).thenReturn(userDTO);

            Page<AppUserDTO> result = userService.getUserByIdWithPagination(1L,pageable);

            assertNotNull(result);
            assertEquals(1,result.getContent().size());
            assertEquals(userDTO.getUserId(), result.getContent().get(0).getUserId());

            verify(userRepository).findByUserId(1L,pageable);
            verify(appUserMapper).toDTO(user);
        }

        @Test
        @DisplayName("Search users with pagination")
        void searchUsers(){
            Page<AppUser> page = new PageImpl<>(List.of(user));
            when(userRepository.searchUsers("test",pageable)).thenReturn(page);

            Page<AppUserDTO> result = userService.searchUsersWithPagination("test",pageable);

            assertNotNull(result);
            assertEquals(1,result.getContent().size());

            verify(userRepository).searchUsers("test",pageable);
            verify(appUserMapper).toDTO(user);
        }

        @Test
        @DisplayName("Search user by client id")
        void shouldSearchUsersByClient(){
            Page<AppUser> page = new PageImpl<>(List.of(user));
            when(userRepository.searchUsersByClient("test",1L,pageable)).thenReturn(page);
            when(appUserMapper.toDTO(user)).thenReturn(userDTO);

            Page<AppUserDTO> result = userService.searchUsersByClient("test",1L,pageable);

            assertNotNull(result);
            assertEquals(1,result.getContent().size());

            verify(userRepository).searchUsersByClient("test",1L,pageable);
            verify(appUserMapper).toDTO(user);
        }
    }

    @Nested
    @DisplayName("Change Password test")
    class ChangePassword{
        @Test
        @DisplayName("Should change user's password successfully")
        void shouldChangePassword(){
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("123456",user.getPassword())).thenReturn(true);
            when(passwordEncoder.matches("200407",user.getPassword())).thenReturn(false);
            when(passwordEncoder.encode("200407")).thenReturn("encodedPass");

            userService.changePassword(1L,"123456", "200407");

            assertEquals("encodedPass",user.getPassword());

            verify(userRepository).save(user);
            verify(emailService).sendPasswordChangedEmail(user);
        }

        @Test
        @DisplayName("should throw user not found exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {

            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class, () -> {
                userService.changePassword(1L, "123456", "200407");
            });

            assertEquals("User not found", ex.getMessage());

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when current password incorrect")
        void shouldThrowExceptionWhenCurrentPasswordIncorrect() {

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrongPass", user.getPassword())).thenReturn(false);

            RuntimeException ex = assertThrows(RuntimeException.class, () -> {
                userService.changePassword(1L, "wrongPass", "newPass");
            });

            assertEquals("Current password is incorrect", ex.getMessage());

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when new password is same as old password")
        void shouldThrowExceptionWhenNewPasswordSameAsOld() {

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("123456", user.getPassword())).thenReturn(true);
            when(passwordEncoder.matches("123456", user.getPassword())).thenReturn(true);

            RuntimeException ex = assertThrows(RuntimeException.class, () -> {
                userService.changePassword(1L, "123456", "123456");
            });

            assertEquals("New password must be different from current password", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when the new password is less than 6 characters")
        void shouldThrowExceptionWhenPasswordTooShort() {

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("oldPass", user.getPassword())).thenReturn(true);
            when(passwordEncoder.matches("123", user.getPassword())).thenReturn(false);

            RuntimeException ex = assertThrows(RuntimeException.class, () -> {
                userService.changePassword(1L, "oldPass", "123");
            });

            assertEquals("New password must be at least 6 characters", ex.getMessage());
        }
    }
}
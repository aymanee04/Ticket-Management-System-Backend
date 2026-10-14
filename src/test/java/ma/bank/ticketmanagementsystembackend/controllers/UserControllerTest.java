package ma.bank.ticketmanagementsystembackend.controllers;

import ma.bank.ticketmanagementsystembackend.dtos.*;
import ma.bank.ticketmanagementsystembackend.dtos.dto.AppUserDTO;
import ma.bank.ticketmanagementsystembackend.entities.Role;
import ma.bank.ticketmanagementsystembackend.exceptions.BusinessException;
import ma.bank.ticketmanagementsystembackend.exceptions.InvalidPasswordException;
import ma.bank.ticketmanagementsystembackend.services.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Controller Unit Tests")
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserController userController;

    private AppUserDTO userDTO() {
        return AppUserDTO.builder()
                .userId(1L)
                .name("John Doe")
                .email("john@test.com")
                .build();
    }

    @Test
    @DisplayName("Should create user successfully")
    void shouldCreateUserSuccessfully() {
        CreateUserRequest request = new CreateUserRequest();

        AppUserDTO user = userDTO();

        when(userService.createUser(request))
                .thenReturn(user);

        ResponseEntity<AppUserDTO> response =
                userController.createUser(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(user, response.getBody());

        verify(userService).createUser(request);
    }

    @Test
    @DisplayName("Should update user successfully")
    void shouldUpdateUserSuccessfully() {
        Long userId = 1L;

        UpdateUserRequest request = new UpdateUserRequest();

        AppUserDTO user = userDTO();

        when(userService.updateUser(userId, request))
                .thenReturn(user);

        ResponseEntity<AppUserDTO> response =
                userController.updateUser(userId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(user, response.getBody());

        verify(userService)
                .updateUser(userId, request);
    }

    @Test
    @DisplayName("Should get user successfully")
    void shouldGetUserSuccessfully() {
        AppUserDTO user = userDTO();

        when(userService.getUserById(1L))
                .thenReturn(user);

        ResponseEntity<AppUserDTO> response =
                userController.getUser(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(user, response.getBody());

        verify(userService).getUserById(1L);
    }

    @Test
    @DisplayName("Should get users by client successfully")
    void shouldGetUsersByClientSuccessfully() {
        List<AppUserDTO> users = List.of(userDTO());

        when(userService.getUserByClientId(10L))
                .thenReturn(users);

        ResponseEntity<List<AppUserDTO>> response =
                userController.getUserByClientId(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(users, response.getBody());

        verify(userService).getUserByClientId(10L);
    }

    @Test
    @DisplayName("Should return all available roles")
    void shouldReturnAllAvailableRoles() {
        ResponseEntity<List<Role>> response =
                userController.getAllRoles();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        assertEquals(
                List.of(Role.values()),
                response.getBody()
        );
    }

    @Test
    @DisplayName("Should get all users successfully")
    void shouldGetAllUsersSuccessfully() {
        List<AppUserDTO> users = List.of(userDTO());

        when(userService.getAllUsers())
                .thenReturn(users);

        ResponseEntity<List<AppUserDTO>> response =
                userController.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(users, response.getBody());

        verify(userService).getAllUsers();
    }

    @Test
    @DisplayName("Should get paginated users successfully")
    void shouldGetPaginatedUsersSuccessfully() {
        PagedResponse<AppUserDTO> pagedResponse =
                new PagedResponse<>(
                        List.of(userDTO()),
                        0,
                        10,
                        1,
                        1,
                        true,
                        true
                );

        when(authentication.getName())
                .thenReturn("admin@test.com");

        when(userService.getUsersPagedForCurrentUser(
                "admin@test.com",
                0,
                10,
                "name",
                "asc"
        )).thenReturn(pagedResponse);

        ResponseEntity<PagedResponse<AppUserDTO>> response =
                userController.getAllUsersPaged(
                        authentication,
                        0,
                        10,
                        "name",
                        "asc"
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pagedResponse, response.getBody());

        verify(userService).getUsersPagedForCurrentUser(
                "admin@test.com",
                0,
                10,
                "name",
                "asc"
        );
    }

    @Test
    @DisplayName("Should get users by client with pagination successfully")
    void shouldGetUsersByClientWithPaginationSuccessfully() {
        AppUserDTO user = userDTO();

        PagedResponse<AppUserDTO> expected =
                new PagedResponse<>(
                        List.of(user),
                        0,
                        10,
                        1,
                        1,
                        true,
                        true
                );

        when(userService.getUsersByClientIdWithPagination(
                eq(10L),
                any()
        )).thenReturn(
                new org.springframework.data.domain.PageImpl<>(
                        List.of(user),
                        PageRequest.of(
                                0,
                                10,
                                Sort.by(Sort.Direction.ASC, "name")
                        ),
                        1
                )
        );

        ResponseEntity<PagedResponse<AppUserDTO>> response =
                userController.getUsersByClientIdPaged(
                        10L,
                        0,
                        10,
                        "name",
                        "asc"
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        assertEquals(
                expected.getContent(),
                response.getBody().getContent()
        );

        assertEquals(
                expected.getPageNumber(),
                response.getBody().getPageNumber()
        );

        assertEquals(
                expected.getPageSize(),
                response.getBody().getPageSize()
        );

        verify(userService)
                .getUsersByClientIdWithPagination(
                        eq(10L),
                        any()
                );
    }

    @Test
    @DisplayName("Should search users with pagination successfully")
    void shouldSearchUsersWithPaginationSuccessfully() {
        PagedResponse<AppUserDTO> pagedResponse =
                new PagedResponse<>(
                        List.of(userDTO()),
                        0,
                        10,
                        1,
                        1,
                        true,
                        true
                );

        when(authentication.getName())
                .thenReturn("user@test.com");

        when(userService.searchUsersPagedForCurrentUser(
                "user@test.com",
                "john",
                0,
                10
        )).thenReturn(pagedResponse);

        ResponseEntity<PagedResponse<AppUserDTO>> response =
                userController.searchUsersPaged(
                        authentication,
                        "john",
                        0,
                        10
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pagedResponse, response.getBody());

        verify(userService).searchUsersPagedForCurrentUser(
                "user@test.com",
                "john",
                0,
                10
        );
    }

    @Test
    @DisplayName("Should change password successfully")
    void shouldChangePasswordSuccessfully() {
        Long userId = 1L;

        ChangePasswordRequest request =
                new ChangePasswordRequest();

        request.setCurrentPassword("oldPassword");
        request.setNewPassword("newPassword");
        request.setConfirmPassword("newPassword");

        AppUserDTO currentUser = userDTO();

        when(authentication.getName())
                .thenReturn("john@test.com");

        when(userService.getUserByEmail("john@test.com"))
                .thenReturn(currentUser);

        doNothing()
                .when(userService)
                .changePassword(
                        userId,
                        "oldPassword",
                        "newPassword"
                );

        ResponseEntity<Map<String, String>> response =
                userController.changePassword(
                        authentication,
                        userId,
                        request
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        assertEquals(
                "Password changed successfully",
                response.getBody().get("message")
        );

        verify(userService).changePassword(
                userId,
                "oldPassword",
                "newPassword"
        );
    }

    @Test
    @DisplayName("Should reject password change when user changes another user's password")
    void shouldRejectPasswordChangeForAnotherUser() {
        Long requestedUserId = 2L;

        ChangePasswordRequest request =
                new ChangePasswordRequest();

        request.setCurrentPassword("oldPassword");
        request.setNewPassword("newPassword");
        request.setConfirmPassword("newPassword");

        AppUserDTO currentUser = userDTO();
        currentUser.setUserId(1L);

        when(authentication.getName())
                .thenReturn("john@test.com");

        when(userService.getUserByEmail("john@test.com"))
                .thenReturn(currentUser);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> userController.changePassword(
                                authentication,
                                requestedUserId,
                                request
                        )
                );

        assertEquals(
                "You can only change your own password",
                exception.getMessage()
        );

        verify(userService, never())
                .changePassword(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should reject password change when passwords do not match")
    void shouldRejectPasswordChangeWhenPasswordsDoNotMatch() {
        Long userId = 1L;

        ChangePasswordRequest request =
                new ChangePasswordRequest();

        request.setCurrentPassword("oldPassword");
        request.setNewPassword("newPassword");
        request.setConfirmPassword("differentPassword");

        AppUserDTO currentUser = userDTO();
        currentUser.setUserId(userId);

        when(authentication.getName())
                .thenReturn("john@test.com");

        when(userService.getUserByEmail("john@test.com"))
                .thenReturn(currentUser);

        InvalidPasswordException exception =
                assertThrows(
                        InvalidPasswordException.class,
                        () -> userController.changePassword(
                                authentication,
                                userId,
                                request
                        )
                );

        assertEquals(
                "New passwords do not match",
                exception.getMessage()
        );

        verify(userService, never())
                .changePassword(anyLong(), anyString(), anyString());
    }
}
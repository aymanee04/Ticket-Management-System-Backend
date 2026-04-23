package ma.bank.ticketmanagementsystembackend.services;

import ma.bank.ticketmanagementsystembackend.dtos.CreateUserRequest;
import ma.bank.ticketmanagementsystembackend.dtos.PagedResponse;
import ma.bank.ticketmanagementsystembackend.dtos.UpdateUserRequest;
import ma.bank.ticketmanagementsystembackend.dtos.dto.AppUserDTO;
import ma.bank.ticketmanagementsystembackend.entities.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
public interface UserService {

    AppUserDTO createUser(CreateUserRequest request);
    AppUserDTO updateUser(Long id, UpdateUserRequest request);
    void changePassword(Long userId, String currentPassword, String newPassword);
    void addRoleToUser(String name, String roleName);

    AppUserDTO getUserById(long id);
    List<AppUserDTO> getUserByClientName(String clientName);
    List<AppUserDTO> getUserByClientId(Long id);
    List<AppUserDTO> getAllUsers();
    AppUser loadUserByEmail(String email);
    AppUserDTO getUserByEmail(String email);

    Page<AppUserDTO> getAllUsersWithPagination(Pageable pageable);
    Page<AppUserDTO> getUsersByClientIdWithPagination(Long id, Pageable pageable);
    Page<AppUserDTO> getUserByIdWithPagination(Long id, Pageable pageable);
    Page<AppUserDTO> searchUsersWithPagination(String searchTerm, Pageable pageable);
    Page<AppUserDTO> searchUsersByClient(String searchTerm, Long clientId, Pageable pageable);

    PagedResponse<AppUserDTO> getUsersPagedForCurrentUser(String email, int page, int size, String sortBy, String direction);
    PagedResponse<AppUserDTO> searchUsersPagedForCurrentUser(String email, String searchTerm, int page, int size);
}

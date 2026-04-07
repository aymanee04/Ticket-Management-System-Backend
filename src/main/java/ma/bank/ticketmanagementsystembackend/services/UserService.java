package ma.bank.ticketmanagementsystembackend.services;

import ma.bank.ticketmanagementsystembackend.dtos.UpdateUserRequest;
import ma.bank.ticketmanagementsystembackend.dtos.dto.AppUserDTO;
import ma.bank.ticketmanagementsystembackend.entities.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
public interface UserService {
    public AppUserDTO createUser(AppUserDTO userDTO);
    public AppUserDTO updateUser(Long id, UpdateUserRequest request);
    public AppUserDTO createUserInternal(AppUser user);
    public AppUserDTO getUserById(long id);
    public List<AppUserDTO> getUserByClientName(String clientName);
    public List<AppUserDTO> getUserByClientId(Long id);
    public List<AppUserDTO> getAllUsers();
    void addRoleToUser(String name, String role);
    public AppUser loadUserByEmail(String email);

    public Page<AppUserDTO> getAllUsersWithPagination(Pageable pageable);
    public Page<AppUserDTO> getUsersByClientIdWithPagination(Long id, Pageable pageable);
    public Page<AppUserDTO> searchUsersWithPagination(String searchTerm, Pageable pageable);
    Page<AppUserDTO> getUserByIdWithPagination(Long id, Pageable pageable);
    Page<AppUserDTO> searchUsersByClient(String searchTerm, Long clientId, Pageable pageable);
    void changePassword(Long userId, String currentPassword, String newPassword);
}

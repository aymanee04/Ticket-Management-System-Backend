package ma.bank.ticketmanagementsystembackend.services;

import lombok.AllArgsConstructor;
import ma.bank.ticketmanagementsystembackend.dtos.UpdateUserRequest;
import ma.bank.ticketmanagementsystembackend.dtos.dto.AppUserDTO;
import ma.bank.ticketmanagementsystembackend.entities.AppUser;
import ma.bank.ticketmanagementsystembackend.entities.Role;
import ma.bank.ticketmanagementsystembackend.mappers.AppUserMapper;
import ma.bank.ticketmanagementsystembackend.repositories.ClientRepository;
import ma.bank.ticketmanagementsystembackend.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final AppUserMapper appUserMapper;
    private  PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    @Transactional
    public AppUserDTO createUser(AppUserDTO userDTO) {
        AppUser user = appUserMapper.toEntity(userDTO);

        if (userDTO.getClientId() != null) {
            user.setClient(clientRepository.findById(userDTO.getClientId())
                    .orElseThrow(() -> new RuntimeException("Client not found")));
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (user.getRoles() == null) {
            user.setRoles(new ArrayList<>());
        }

        if (!user.getRoles().contains(Role.USER)) {
            user.getRoles().add(Role.USER);
        }

        AppUser savedUser = userRepository.save(user);
        return appUserMapper.toDTO(savedUser);
    }

    @Override
    public AppUserDTO updateUser(Long id, UpdateUserRequest request) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setJobTitle(request.getJobTitle());

        AppUser updatedUser = userRepository.save(user);
        return appUserMapper.toDTO(updatedUser);
    }

    @Override
    @Transactional
    public AppUserDTO createUserInternal(AppUser user) {

        if (user.getClient() != null && user.getClient().getClientId() != null) {
            user.setClient(clientRepository.findById(user.getClient().getClientId())
                    .orElseThrow(() -> new RuntimeException("Client not found")));
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (user.getRoles() == null) {
            user.setRoles(new ArrayList<>());
        }

        if (!user.getRoles().contains(Role.USER)) {
            user.getRoles().add(Role.USER);
        }

        emailService.sendWelcomeEmail(user,user.getPassword());
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        AppUser savedUser = userRepository.save(user);
        return appUserMapper.toDTO(savedUser);
    }

    @Override
    public AppUserDTO getUserById(long id) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return appUserMapper.toDTO(user);
    }

    @Override
    public List<AppUserDTO> getUserByClientName(String clientName) {
        return userRepository.findByClient_Name(clientName).stream()
                .map(appUserMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppUserDTO> getUserByClientId(Long id) {
        return userRepository.findByClient_ClientId(id).stream()
                .map(appUserMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppUserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(appUserMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void addRoleToUser(String name, String roleName) {
        AppUser appUser = userRepository.findByName(name);
        try {
            if (appUser == null) throw new RuntimeException("User not found");
            Role roleEnum = Role.valueOf(roleName.toUpperCase());
            if (!appUser.getRoles().contains(roleEnum)) {
                appUser.getRoles().add(roleEnum);
            }
            AppUser updatedUser = userRepository.save(appUser);
            appUserMapper.toDTO(updatedUser);
        }catch (IllegalArgumentException e){
            throw new RuntimeException("Role " + roleName + " does not exist");
        }
    }

    @Override
    public AppUser loadUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    @Override
    public Page<AppUserDTO> getAllUsersWithPagination(Pageable pageable) {
        return userRepository.findAll(pageable).map(appUserMapper::toDTO);
    }

    @Override
    public Page<AppUserDTO> getUsersByClientIdWithPagination(Long id, Pageable pageable) {
        return userRepository.findByClient_ClientId(id, pageable).map(appUserMapper::toDTO);
    }

    @Override
    public Page<AppUserDTO> searchUsersWithPagination(String searchTerm, Pageable pageable) {
        return userRepository.searchUsers(searchTerm, pageable).map(appUserMapper::toDTO);
    }

    @Override
    public Page<AppUserDTO> getUserByIdWithPagination(Long id, Pageable pageable) {
        return  userRepository.findByUserId(id,pageable).map(appUserMapper::toDTO);
    }

    @Override
    public Page<AppUserDTO> searchUsersByClient(String searchTerm, Long clientId, Pageable pageable) {
        return userRepository.searchUsersByClient(searchTerm, clientId, pageable)
                .map(appUserMapper::toDTO);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new RuntimeException("New password must be different from current password");
        }

        if (newPassword.length() < 6) {
            throw new RuntimeException("New password must be at least 6 characters");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        emailService.sendPasswordChangedEmail(user);
        System.out.println("Password changed successfully for user: " + user.getEmail());
    }
}

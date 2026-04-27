package ma.bank.ticketmanagementsystembackend.services;

import lombok.RequiredArgsConstructor;
import ma.bank.ticketmanagementsystembackend.dtos.CreateUserRequest;
import ma.bank.ticketmanagementsystembackend.dtos.PagedResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final AppUserMapper appUserMapper;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;


    @Override
    @Transactional
    public AppUserDTO createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }

        Client client = null;
        if (request.getClientId() != null) {
            client = clientRepository.findById(request.getClientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
        }

        List<Role> roles = (request.getRoles() != null && !request.getRoles().isEmpty())
                ? new ArrayList<>(request.getRoles())
                : new ArrayList<>(List.of(Role.USER));

        if (!roles.contains(Role.USER)) {
            roles.add(Role.USER);
        }

        AppUser user = AppUser.builder()
                .name(request.getName())
                .email(request.getEmail())
                .jobTitle(request.getJobTitle())
                .password(request.getPassword())
                .roles(roles)
                .client(client)
                .build();

        emailService.sendWelcomeEmail(user, user.getPassword());
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return appUserMapper.toDTO(userRepository.save(user));
    }

    @Override
    @Transactional
    public AppUserDTO updateUser(Long id, UpdateUserRequest request) {
        AppUser user = findUserById(id);
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setJobTitle(request.getJobTitle());
        return appUserMapper.toDTO(userRepository.save(user));
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        AppUser user = findUserById(userId);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new InvalidPasswordException("Current password is incorrect");
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new InvalidPasswordException("New password must be different from current password");
        }

        if (newPassword.length() < 6) {
            throw new InvalidPasswordException("New password must be at least 6 characters");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        emailService.sendPasswordChangedEmail(user);
    }

    @Override
    public void addRoleToUser(String name, String roleName) {
        AppUser user = userRepository.findByName(name);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }
        try {
            Role role = Role.valueOf(roleName.toUpperCase());
            if (!user.getRoles().contains(role)) {
                user.getRoles().add(role);
                userRepository.save(user);
            }
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Role " + roleName + " does not exist");
        }
    }

    @Override
    public AppUserDTO getUserById(long id) {
        return appUserMapper.toDTO(findUserById(id));
    }

    @Override
    public List<AppUserDTO> getUserByClientName(String clientName) {
        return userRepository.findByClient_Name(clientName)
                .stream().map(appUserMapper::toDTO).toList();
    }

    @Override
    public List<AppUserDTO> getUserByClientId(Long id) {
        return userRepository.findByClient_ClientId(id)
                .stream().map(appUserMapper::toDTO).toList();
    }

    @Override
    public List<AppUserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream().map(appUserMapper::toDTO).toList();
    }

    @Override
    @Transactional
    public AppUser loadUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with this email: " + email));
    }

    @Override
    public AppUserDTO getUserByEmail(String email) {
        return appUserMapper.toDTO(loadUserByEmail(email));
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
    public Page<AppUserDTO> getUserByIdWithPagination(Long id, Pageable pageable) {
        return userRepository.findByUserId(id, pageable).map(appUserMapper::toDTO);
    }

    @Override
    public Page<AppUserDTO> searchUsersWithPagination(String searchTerm, Pageable pageable) {
        return userRepository.searchUsers(searchTerm, pageable).map(appUserMapper::toDTO);
    }

    @Override
    public Page<AppUserDTO> searchUsersByClient(String searchTerm, Long clientId, Pageable pageable) {
        return userRepository.searchUsersByClient(searchTerm, clientId, pageable)
                .map(appUserMapper::toDTO);
    }

    @Override
    public PagedResponse<AppUserDTO> getUsersPagedForCurrentUser(
            String email, int page, int size, String sortBy, String direction) {

        Pageable pageable = buildPageable(page, size, sortBy, direction);
        AppUser currentUser = loadUserByEmail(email);

        Page<AppUserDTO> result;
        if (currentUser.getRoles().contains(Role.ADMIN)) {
            result = getAllUsersWithPagination(pageable);
        } else if (currentUser.getRoles().contains(Role.MANAGER)) {
            result = currentUser.getClient() != null
                    ? getUsersByClientIdWithPagination(currentUser.getClient().getClientId(), pageable)
                    : Page.empty(pageable);
        } else {
            result = getUserByIdWithPagination(currentUser.getUserId(), pageable);
        }

        return toPagedResponse(result);
    }

    @Override
    public PagedResponse<AppUserDTO> searchUsersPagedForCurrentUser(
            String email, String searchTerm, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        AppUser currentUser = loadUserByEmail(email);

        Page<AppUserDTO> result;
        if (currentUser.getRoles().contains(Role.ADMIN)) {
            result = searchUsersWithPagination(searchTerm, pageable);
        } else if (currentUser.getRoles().contains(Role.MANAGER)) {
            result = currentUser.getClient() != null
                    ? searchUsersByClient(searchTerm, currentUser.getClient().getClientId(), pageable)
                    : Page.empty(pageable);
        } else {
            result = Page.empty(pageable);
        }

        return toPagedResponse(result);
    }

    private AppUser findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Pageable buildPageable(int page, int size, String sortBy, String direction) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
    }

    private <T> PagedResponse<T> toPagedResponse(Page<T> p) {
        return new PagedResponse<>(
                p.getContent(), p.getNumber(), p.getSize(),
                p.getTotalElements(), p.getTotalPages(),
                p.isLast(), p.isFirst()
        );
    }
}
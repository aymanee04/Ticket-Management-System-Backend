package ma.bank.ticketmanagementsystembackend.controllers;

import ma.bank.ticketmanagementsystembackend.dtos.ChangePasswordRequest;
import ma.bank.ticketmanagementsystembackend.dtos.CreateUserRequest;
import ma.bank.ticketmanagementsystembackend.dtos.PagedResponse;
import ma.bank.ticketmanagementsystembackend.dtos.UpdateUserRequest;
import ma.bank.ticketmanagementsystembackend.dtos.dto.AppUserDTO;
import ma.bank.ticketmanagementsystembackend.entities.AppUser;
import ma.bank.ticketmanagementsystembackend.entities.Client;
import ma.bank.ticketmanagementsystembackend.entities.Role;
import ma.bank.ticketmanagementsystembackend.repositories.UserRepository;
import ma.bank.ticketmanagementsystembackend.services.EmailService;
import ma.bank.ticketmanagementsystembackend.services.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public UserController(UserService userService, UserRepository userRepository, EmailService emailService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<AppUserDTO> createUser(@RequestBody CreateUserRequest request) {
        AppUser newUser = AppUser.builder()
                .name(request.getName())
                .email(request.getEmail())
                .jobTitle(request.getJobTitle())
                .password(request.getPassword())
                .roles(request.getRoles() != null ? new ArrayList<>(request.getRoles()) : new ArrayList<>(List.of(Role.USER)))
                .client(request.getClientId() != null ? Client.builder().clientId(request.getClientId()).build() : null)
                .build();

        AppUserDTO createdUser = userService.createUserInternal(newUser);
        return ResponseEntity.ok(createdUser);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('USER')")
    public ResponseEntity<AppUserDTO> updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('USER')")
    public ResponseEntity<AppUserDTO> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/client/{id}")
    @PreAuthorize("hasAnyAuthority('USER')")
    public ResponseEntity<List<AppUserDTO>> getUserByClientId(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserByClientId(id));
    }

    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(Arrays.asList(Role.values()));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('USER')")
    public ResponseEntity<List<AppUserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping(params = "page")
    @PreAuthorize("hasAnyAuthority('USER')")
    public ResponseEntity<PagedResponse<AppUserDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AppUser currentUser = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Page<AppUserDTO> userPage;

        if (currentUser.getRoles().contains(Role.ADMIN)) {
            userPage = userService.getAllUsersWithPagination(pageable);
        } else if (currentUser.getRoles().contains(Role.MANAGER)) {
            if (currentUser.getClient() != null) {
                userPage = userService.getUsersByClientIdWithPagination(
                        currentUser.getClient().getClientId(),
                        pageable
                );
            } else {
                userPage = Page.empty();
            }
        } else {
            userPage = userService.getUserByIdWithPagination(currentUser.getUserId(), pageable);
        }

        PagedResponse<AppUserDTO> response = new PagedResponse<>(
                userPage.getContent(),
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.isLast(),
                userPage.isFirst()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/client/{id}", params = "page")
    @PreAuthorize("hasAnyAuthority('USER')")
    public ResponseEntity<PagedResponse<AppUserDTO>> getUserByClientId(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<AppUserDTO> userDTOPage = userService.getUsersByClientIdWithPagination(id,pageable);
        PagedResponse<AppUserDTO> response = new PagedResponse<>(
                userDTOPage.getContent(),
                userDTOPage.getNumber(),
                userDTOPage.getSize(),
                userDTOPage.getTotalElements(),
                userDTOPage.getTotalPages(),
                userDTOPage.isLast(),
                userDTOPage.isFirst()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/search", params = "page")
    @PreAuthorize("hasAnyAuthority('USER')")
    public ResponseEntity<PagedResponse<AppUserDTO>> searchUsers(
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AppUser currentUser = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Page<AppUserDTO> userDTOPage;

        if (currentUser.getRoles().contains(Role.ADMIN)) {
            userDTOPage = userService.searchUsersWithPagination(searchTerm, pageable);
        } else if (currentUser.getRoles().contains(Role.MANAGER)) {
            if (currentUser.getClient() != null) {
                userDTOPage = userService.searchUsersByClient(
                        searchTerm,
                        currentUser.getClient().getClientId(),
                        pageable
                );
            } else {
                userDTOPage = Page.empty();
            }
        } else {
            userDTOPage = Page.empty();
        }

        PagedResponse<AppUserDTO> response = new PagedResponse<>(
                userDTOPage.getContent(),
                userDTOPage.getNumber(),
                userDTOPage.getSize(),
                userDTOPage.getTotalElements(),
                userDTOPage.getTotalPages(),
                userDTOPage.isLast(),
                userDTOPage.isFirst()
        );
        return ResponseEntity.ok(response);
    }


    @PutMapping("/{id}/change-password")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<Map<String, String>> changePassword(
            @PathVariable Long id,
            @RequestBody ChangePasswordRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AppUser currentUser = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!currentUser.getUserId().equals(id)) {
            throw new RuntimeException("You can only change your own password");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("New passwords do not match");
        }

        userService.changePassword(id, request.getCurrentPassword(), request.getNewPassword());

        Map<String, String> response = new HashMap<>();
        response.put("message", "Password changed successfully");
        return ResponseEntity.ok(response);
    }
}


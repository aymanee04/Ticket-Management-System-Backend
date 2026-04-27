package ma.bank.ticketmanagementsystembackend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.bank.ticketmanagementsystembackend.dtos.ChangePasswordRequest;
import ma.bank.ticketmanagementsystembackend.dtos.CreateUserRequest;
import ma.bank.ticketmanagementsystembackend.dtos.PagedResponse;
import ma.bank.ticketmanagementsystembackend.dtos.UpdateUserRequest;
import ma.bank.ticketmanagementsystembackend.dtos.dto.AppUserDTO;
import ma.bank.ticketmanagementsystembackend.entities.Role;
import ma.bank.ticketmanagementsystembackend.exceptions.BusinessException;
import ma.bank.ticketmanagementsystembackend.exceptions.InvalidPasswordException;
import ma.bank.ticketmanagementsystembackend.services.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AppUserDTO> createUser(@RequestBody @Valid CreateUserRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.createUser(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<AppUserDTO> updateUser(
            @PathVariable Long id,
            @RequestBody @Valid UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<AppUserDTO> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/client/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<List<AppUserDTO>> getUserByClientId(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserByClientId(id));
    }

    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(Arrays.asList(Role.values()));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<List<AppUserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping(params = "page")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<PagedResponse<AppUserDTO>> getAllUsersPaged(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(
                userService.getUsersPagedForCurrentUser(auth.getName(), page, size, sortBy, direction));
    }

    @GetMapping(value = "/client/{id}", params = "page")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<PagedResponse<AppUserDTO>> getUsersByClientIdPaged(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        Pageable pageable = buildPageable(page, size, sortBy, direction);
        return ResponseEntity.ok(
                toPagedResponse(userService.getUsersByClientIdWithPagination(id, pageable)));
    }

    @GetMapping(value = "/search", params = "page")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<PagedResponse<AppUserDTO>> searchUsersPaged(
            Authentication auth,
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                userService.searchUsersPagedForCurrentUser(auth.getName(), searchTerm, page, size));
    }

    @PutMapping("/{id}/change-password")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<Map<String, String>> changePassword(
            Authentication auth,
            @PathVariable Long id,
            @RequestBody @Valid ChangePasswordRequest request) {

        AppUserDTO currentUser = userService.getUserByEmail(auth.getName());

        if (!currentUser.getUserId().equals(id)) {
            throw new BusinessException("You can only change your own password");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidPasswordException("New passwords do not match");
        }

        userService.changePassword(id, request.getCurrentPassword(), request.getNewPassword());

        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    // Private helper

    private <T> PagedResponse<T> toPagedResponse(org.springframework.data.domain.Page<T> p) {
        return new PagedResponse<>(
                p.getContent(), p.getNumber(), p.getSize(),
                p.getTotalElements(), p.getTotalPages(),
                p.isLast(), p.isFirst()
        );
    }
    private Pageable buildPageable(int page, int size, String sortBy, String direction) {
        Sort.Direction dir = direction.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(dir, sortBy));
    }
}
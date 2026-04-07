package ma.bank.ticketmanagementsystembackend.repositories;

import ma.bank.ticketmanagementsystembackend.entities.AppUser;
import ma.bank.ticketmanagementsystembackend.entities.Client;
import ma.bank.ticketmanagementsystembackend.entities.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    AppUser findByName(String name);
    boolean existsByEmail(String email);
    Optional<AppUser> findByEmail(String email);

    @Query("SELECT u FROM AppUser u LEFT JOIN FETCH u.client WHERE u.email = :email")
    Optional<AppUser> findByEmailWithClient(@Param("email") String email);

    List<AppUser> findByClient_Name(String name);
    List<AppUser> findByClient_ClientId(Long id);

    // Search methods
    @Query("""
        SELECT u FROM AppUser u
        WHERE :searchTerm IS NULL
           OR LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
           OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
           OR LOWER(u.jobTitle) LIKE LOWER(CONCAT('%', :searchTerm, '%'))""")
    Page<AppUser> searchUsers(
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );

    @Query("""
        SELECT u FROM AppUser u
        WHERE u.client.clientId = :clientId
        AND (:searchTerm IS NULL
           OR LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
           OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
           OR LOWER(u.jobTitle) LIKE LOWER(CONCAT('%', :searchTerm, '%')))""")
    Page<AppUser> searchUsersByClient(
            @Param("searchTerm") String searchTerm,
            @Param("clientId") Long clientId,
            Pageable pageable
    );

    Page<AppUser> findByClient_ClientId(Long clientId, Pageable pageable);
    Page<AppUser> findByUserId(Long userId, Pageable pageable);
}
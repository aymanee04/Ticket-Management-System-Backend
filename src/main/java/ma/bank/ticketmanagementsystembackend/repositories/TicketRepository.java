package ma.bank.ticketmanagementsystembackend.repositories;


import ma.bank.ticketmanagementsystembackend.entities.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByAssignedTo(AppUser user);

    List<Ticket> findByStatus(TicketStatus status);

    List<Ticket> findByClients_ClientId(Long clientId);

    List<Ticket> findTicketByCreatedBy_UserId(Long userId);
//  Page<Ticket> findByClients_ClientId(Long clientId, Pageable pageable);


   //####################################################//
    Page<Ticket> findByStatus(TicketStatus status, Pageable pageable);

    @Query("SELECT DISTINCT t FROM Ticket t " +
            "LEFT JOIN t.clients c " +
            "LEFT JOIN t.createdBy u " +
            "WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.company) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))  "
    )
    Page<Ticket> searchTickets(@Param("searchTerm") String searchTerm, Pageable pageable);
   //####################################################//


    @Query("SELECT DISTINCT t FROM Ticket t " +
            "LEFT JOIN t.clients c " +
            "LEFT JOIN t.createdBy u " +
            "WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.company) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))  "
    )

    List<Ticket> searchTickets(@Param("searchTerm") String searchTerm);


    Page<Ticket> findByClients_ClientId(Long clientId, Pageable pageable);

    @Query("SELECT DISTINCT t FROM Ticket t " +
            "LEFT JOIN t.clients c " +
            "LEFT JOIN t.createdBy u " +
            "WHERE c.clientId = :clientId AND (" +
            "LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Ticket> searchTicketsByClient(
            @Param("searchTerm") String searchTerm,
            @Param("clientId") Long clientId,
            Pageable pageable
    );

    Page<Ticket> findByStatusAndClients_ClientId(
            TicketStatus status,
            Long clientId,
            Pageable pageable
    );

//    @Modifying
//    @Query("""
//            DELETE FROM Ticket t
//            WHERE t.status = :#{#status}
//              AND t.scheduledDeleteAt IS NOT NULL
//              AND t.scheduledDeleteAt < :cutoff
//            """)
//    int deleteExpiredCancelledTickets(
//            @Param("cutoff") LocalDateTime cutoff,
//            @Param("status") TicketStatus status
//    );
//
//    // Convenience overload called by the scheduler (always passes CANCELLED)
//    default int deleteExpiredCancelledTickets(LocalDateTime cutoff) {
//        return deleteExpiredCancelledTickets(cutoff, TicketStatus.CANCELLED);
//    }

}

package ma.bank.ticketmanagementsystembackend.repositories;

import ma.bank.ticketmanagementsystembackend.entities.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment,Long> {
    List<Attachment> findByTicket_TicketId(Long ticketId);
}

package ma.bank.ticketmanagementsystembackend.dtos.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ma.bank.ticketmanagementsystembackend.entities.TicketStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketDTO {
    private Long ticketId;
    private String title;
    private String description;
    private TicketStatus status;
    //@JsonFormat(pattern = "yyyy-MM-dd''HH:mm:ss")
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime validatedAt;
    private String validationComment;

    private Long createdById;
    private String createdByName;
    private Long assignedToId;
    private String assignedToName;
    private Long validatedById;

    private List<AttachmentDTO> attachments;
    private Set<Long> clientIds;
    private Set<String> clientName;
}
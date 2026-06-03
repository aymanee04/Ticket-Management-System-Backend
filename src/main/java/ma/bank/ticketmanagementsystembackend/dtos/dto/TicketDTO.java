package ma.bank.ticketmanagementsystembackend.dtos.dto;

import lombok.*;
import ma.bank.ticketmanagementsystembackend.entities.TicketStatus;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
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
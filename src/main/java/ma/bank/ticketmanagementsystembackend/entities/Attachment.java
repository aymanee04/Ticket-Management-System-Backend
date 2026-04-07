package ma.bank.ticketmanagementsystembackend.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "attachments")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "ticket")
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long attachmentId;

    private String fileName;
    private String fileType;
    private Long fileSize;

    private String cloudinaryUrl;
    private String cloudinaryPublicId;

    private LocalDateTime uploadedAt;
    @JsonProperty(access =  JsonProperty.Access.WRITE_ONLY)
    @ManyToOne
    @JoinColumn(name = "ticketId")
    private Ticket ticket;
}


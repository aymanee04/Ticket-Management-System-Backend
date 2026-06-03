package ma.bank.ticketmanagementsystembackend.dtos.dto;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttachmentDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Long attachmentId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String cloudinaryUrl;
    private String cloudinaryPublicId;
    private LocalDateTime uploadedAt;
    private Long ticketId;
}
package ma.bank.ticketmanagementsystembackend.dtos.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttachmentDTO {
    private Long attachmentId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String cloudinaryUrl;
    private String cloudinaryPublicId;
    private LocalDateTime uploadedAt;
    private Long ticketId;
}
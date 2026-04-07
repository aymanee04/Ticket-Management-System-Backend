package ma.bank.ticketmanagementsystembackend.mappers;

import ma.bank.ticketmanagementsystembackend.dtos.dto.AttachmentDTO;
import ma.bank.ticketmanagementsystembackend.entities.Attachment;
import org.springframework.stereotype.Component;

@Component
public class AttachmentMapper {

    public AttachmentDTO toDTO(Attachment attachment) {
        if (attachment == null) {
            return null;
        }

        return AttachmentDTO.builder()
                .attachmentId(attachment.getAttachmentId())
                .fileName(attachment.getFileName())
                .fileType(attachment.getFileType())
                .fileSize(attachment.getFileSize())
                .cloudinaryUrl(attachment.getCloudinaryUrl())
                .cloudinaryPublicId(attachment.getCloudinaryPublicId())
                .uploadedAt(attachment.getUploadedAt())
                .ticketId(attachment.getTicket() != null ? attachment.getTicket().getTicketId() : null)
                .build();
    }

    public Attachment toEntity(AttachmentDTO dto) {
        if (dto == null) {
            return null;
        }

        return Attachment.builder()
                .attachmentId(dto.getAttachmentId())
                .fileName(dto.getFileName())
                .fileType(dto.getFileType())
                .fileSize(dto.getFileSize())
                .cloudinaryUrl(dto.getCloudinaryUrl())
                .cloudinaryPublicId(dto.getCloudinaryPublicId())
                .uploadedAt(dto.getUploadedAt())
                .build();
    }
}
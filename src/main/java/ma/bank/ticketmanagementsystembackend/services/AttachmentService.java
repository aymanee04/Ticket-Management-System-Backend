package ma.bank.ticketmanagementsystembackend.services;

import ma.bank.ticketmanagementsystembackend.dtos.DownloadedFile;
import ma.bank.ticketmanagementsystembackend.dtos.dto.AttachmentDTO;
import ma.bank.ticketmanagementsystembackend.entities.AppUser;
import ma.bank.ticketmanagementsystembackend.entities.Attachment;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface AttachmentService {
    public AttachmentDTO uploadAttachment(Long ticketId , MultipartFile file) throws IOException;
    public List<AttachmentDTO> getAttachmentsByTicket(Long ticketId);
    public void deleteAttachment(Long attachmentId, AppUser user) throws IOException;
    public DownloadedFile downloadAttachment(Long attachmentId) throws IOException;
    AttachmentDTO getAttachmentById(Long id);
}

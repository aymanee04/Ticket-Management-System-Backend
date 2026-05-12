package ma.bank.ticketmanagementsystembackend.services;

import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import ma.bank.ticketmanagementsystembackend.cloudinary.CloudinaryService;
import ma.bank.ticketmanagementsystembackend.dtos.DownloadedFile;
import ma.bank.ticketmanagementsystembackend.dtos.dto.AttachmentDTO;
import ma.bank.ticketmanagementsystembackend.entities.AppUser;
import ma.bank.ticketmanagementsystembackend.entities.Attachment;
import ma.bank.ticketmanagementsystembackend.entities.Role;
import ma.bank.ticketmanagementsystembackend.entities.Ticket;
import ma.bank.ticketmanagementsystembackend.exceptions.BusinessException;
import ma.bank.ticketmanagementsystembackend.exceptions.FileUploadException;
import ma.bank.ticketmanagementsystembackend.exceptions.ResourceNotFoundException;
import ma.bank.ticketmanagementsystembackend.mappers.AttachmentMapper;
import ma.bank.ticketmanagementsystembackend.repositories.AttachmentRepository;
import ma.bank.ticketmanagementsystembackend.repositories.TicketRepository;
import ma.bank.ticketmanagementsystembackend.repositories.UserRepository;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AttachmentServiceImpl implements AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final TicketRepository ticketRepository;
    private final CloudinaryService cloudinaryService;
    private final AttachmentMapper attachmentMapper;
    private final UserRepository userRepository;
    private final Tika tika = new Tika();

    @Override
    public AttachmentDTO uploadAttachment(Long ticketId , MultipartFile file) throws IOException {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("ticket not found"));

        if (file.isEmpty()) throw new FileUploadException("File is empty");

        String detectedType = tika.detect(file.getInputStream(), file.getOriginalFilename());

        List<String> allowedTypes = List.of(
                "image/png",
                "image/jpeg",
                "application/pdf",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        );

        if (!allowedTypes.contains(detectedType)) {
            throw new FileUploadException("Invalid file type: " + detectedType);
        }

        long maxSize = 5 * 1024 * 1024;
        if (file.getSize() > maxSize) throw new FileUploadException("File too large");

        Map<String, String> uploadResult =
                cloudinaryService.uploadFile(file);
        Attachment attachment = Attachment.builder()
                .fileName(file.getOriginalFilename())
                .fileType(detectedType)
                .fileSize(file.getSize())
                .cloudinaryUrl(uploadResult.get("url"))
                .cloudinaryPublicId(uploadResult.get("publicId"))
                .uploadedAt(LocalDateTime.now())
                .ticket(ticket)
                .build();
        Attachment savedAttachment = attachmentRepository.save(attachment);
    return attachmentMapper.toDTO(savedAttachment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttachmentDTO> getAttachmentsByTicket(Long ticketId) {
        return attachmentRepository.findByTicket_TicketId(ticketId).stream()
                .map(attachmentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAttachment(Long attachmentId, AppUser user) throws IOException {

        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));

        AppUser currentUser = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Ticket ticket = attachment.getTicket();

        boolean isOwner = currentUser.equals(ticket.getCreatedBy()) ||
                          currentUser.getRoles().contains(Role.ADMIN);

        if (!isOwner) {
            throw new BusinessException("You don't have permission to delete this attachment");
        }

        attachmentRepository.delete(attachment);
        cloudinaryService.deleteFile(
                attachment.getCloudinaryPublicId()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public DownloadedFile downloadAttachment(Long attachmentId) throws IOException {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));

        URL url = new URL(attachment.getCloudinaryUrl());
        byte[] fileData = url.openStream().readAllBytes();

        return new DownloadedFile(
                fileData,
                attachment.getFileName(),
                attachment.getFileType()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AttachmentDTO getAttachmentById(Long id) {
        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));
        return attachmentMapper.toDTO(attachment);
    }

}


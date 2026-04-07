package ma.bank.ticketmanagementsystembackend.services;

import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import ma.bank.ticketmanagementsystembackend.cloudinary.CloudinaryService;
import ma.bank.ticketmanagementsystembackend.dtos.DownloadedFile;
import ma.bank.ticketmanagementsystembackend.dtos.dto.AttachmentDTO;
import ma.bank.ticketmanagementsystembackend.entities.Attachment;
import ma.bank.ticketmanagementsystembackend.entities.Ticket;
import ma.bank.ticketmanagementsystembackend.mappers.AttachmentMapper;
import ma.bank.ticketmanagementsystembackend.repositories.AttachmentRepository;
import ma.bank.ticketmanagementsystembackend.repositories.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttachmentServiceImpl implements AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final TicketRepository ticketRepository;
    private final CloudinaryService cloudinaryService;
    private final Cloudinary cloudinary;
    private final AttachmentMapper attachmentMapper;

    @Override
    public AttachmentDTO uploadAttachment(Long ticketId , MultipartFile file) throws IOException {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new RuntimeException("ticket not found"));
        Map<String, String> uploadResult =
                cloudinaryService.uploadFile(file);
        Attachment attachment = Attachment.builder()
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
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
    public List<AttachmentDTO> getAttachmentsByTicket(Long ticketId) {
        return attachmentRepository.findByTicket_TicketId(ticketId).stream()
                .map(attachmentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAttachment(Long attachmentId) throws IOException {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));

        cloudinaryService.deleteFile(
                attachment.getCloudinaryPublicId()
        );

        attachmentRepository.delete(attachment);
    }

    @Override
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
    public AttachmentDTO getAttachmentById(Long id) {
        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));
        return attachmentMapper.toDTO(attachment);
    }

}


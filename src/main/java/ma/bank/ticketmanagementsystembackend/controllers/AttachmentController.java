package ma.bank.ticketmanagementsystembackend.controllers;

import ma.bank.ticketmanagementsystembackend.dtos.dto.AttachmentDTO;
import ma.bank.ticketmanagementsystembackend.entities.AppUser;
import ma.bank.ticketmanagementsystembackend.services.AttachmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {

    private static final Logger logger = LoggerFactory.getLogger(AttachmentController.class);
    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @PostMapping("/ticket/{ticketId}")
    @PreAuthorize("hasAnyAuthority('USER' )")
    public ResponseEntity<AttachmentDTO> uploadAttachment(
            @PathVariable Long ticketId,
            @RequestParam("file") MultipartFile file) {
        try {
            AttachmentDTO attachment = attachmentService.uploadAttachment(ticketId, file);
            return ResponseEntity.ok(attachment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/ticket/{ticketId}")
    @PreAuthorize("hasAnyAuthority('USER')")
    public ResponseEntity<List<AttachmentDTO>> getAttachmentsByTicket(@PathVariable Long ticketId) {
        return ResponseEntity.ok(attachmentService.getAttachmentsByTicket(ticketId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('USER')")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long id,@AuthenticationPrincipal AppUser user) throws IOException {
        attachmentService.deleteAttachment(id, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long id) {
        try {
            AttachmentDTO attachment = attachmentService.getAttachmentById(id);

            if (attachment == null) {
                return ResponseEntity.notFound().build();
            }

            URL url = new URL(attachment.getCloudinaryUrl());
            URLConnection connection = url.openConnection();
            InputStream inputStream = connection.getInputStream();

            byte[] fileContent = inputStream.readAllBytes();
            inputStream.close();

            InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(fileContent));

            String contentType = attachment.getFileType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + attachment.getFileName() + "\"")
                    .contentLength(fileContent.length)
                    .body(resource);

        } catch (Exception e) {
            logger.error("Error downloading attachment with id {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
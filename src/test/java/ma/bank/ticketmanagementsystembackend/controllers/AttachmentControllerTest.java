package ma.bank.ticketmanagementsystembackend.controllers;

import ma.bank.ticketmanagementsystembackend.dtos.dto.AttachmentDTO;
import ma.bank.ticketmanagementsystembackend.entities.AppUser;
import ma.bank.ticketmanagementsystembackend.services.AttachmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Attachment Controller Unit Tests")
class AttachmentControllerTest {

    @Mock
    private AttachmentService attachmentService;

    @InjectMocks
    private AttachmentController attachmentController;

    @Test
    @DisplayName("Should upload attachment successfully")
    void shouldUploadAttachmentSuccessfully() throws IOException {
        Long ticketId = 1L;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "test content".getBytes()
        );

        AttachmentDTO attachment = AttachmentDTO.builder()
                .attachmentId(10L)
                .fileName("document.pdf")
                .fileType("application/pdf")
                .build();

        when(attachmentService.uploadAttachment(ticketId, file))
                .thenReturn(attachment);

        ResponseEntity<AttachmentDTO> response =
                attachmentController.uploadAttachment(ticketId, file);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(attachment, response.getBody());

        verify(attachmentService).uploadAttachment(ticketId, file);
    }

    @Test
    @DisplayName("Should return internal server error when upload fails")
    void shouldReturnInternalServerErrorWhenUploadFails() throws IOException {
        Long ticketId = 1L;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "test".getBytes()
        );

        when(attachmentService.uploadAttachment(ticketId, file))
                .thenThrow(new IOException("Upload failed"));

        ResponseEntity<AttachmentDTO> response =
                attachmentController.uploadAttachment(ticketId, file);

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode()
        );

        assertNull(response.getBody());

        verify(attachmentService).uploadAttachment(ticketId, file);
    }

    @Test
    @DisplayName("Should get attachments by ticket successfully")
    void shouldGetAttachmentsByTicketSuccessfully() {
        Long ticketId = 1L;

        List<AttachmentDTO> attachments = List.of(
                AttachmentDTO.builder()
                        .attachmentId(1L)
                        .fileName("file1.pdf")
                        .build(),
                AttachmentDTO.builder()
                        .attachmentId(2L)
                        .fileName("file2.png")
                        .build()
        );

        when(attachmentService.getAttachmentsByTicket(ticketId))
                .thenReturn(attachments);

        ResponseEntity<List<AttachmentDTO>> response =
                attachmentController.getAttachmentsByTicket(ticketId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(attachments, response.getBody());

        verify(attachmentService).getAttachmentsByTicket(ticketId);
    }

    @Test
    @DisplayName("Should delete attachment successfully")
    void shouldDeleteAttachmentSuccessfully() throws IOException {
        Long attachmentId = 1L;
        AppUser user = new AppUser();

        doNothing()
                .when(attachmentService)
                .deleteAttachment(attachmentId, user);

        ResponseEntity<Void> response =
                attachmentController.deleteAttachment(attachmentId, user);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(attachmentService)
                .deleteAttachment(attachmentId, user);
    }

    @Test
    @DisplayName("Should propagate IOException when deleting attachment fails")
    void shouldPropagateIOExceptionWhenDeleteFails() throws IOException {
        Long attachmentId = 1L;
        AppUser user = new AppUser();

        doThrow(new IOException("Delete failed"))
                .when(attachmentService)
                .deleteAttachment(attachmentId, user);

        assertThrows(
                IOException.class,
                () -> attachmentController.deleteAttachment(
                        attachmentId,
                        user
                )
        );

        verify(attachmentService)
                .deleteAttachment(attachmentId, user);
    }

    @Test
    @DisplayName("Should return not found when attachment does not exist")
    void shouldReturnNotFoundWhenAttachmentDoesNotExist() {
        Long attachmentId = 1L;

        when(attachmentService.getAttachmentById(attachmentId))
                .thenReturn(null);

        ResponseEntity<Resource> response =
                attachmentController.downloadAttachment(attachmentId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());

        verify(attachmentService).getAttachmentById(attachmentId);
    }

    @Test
    @DisplayName("Should return internal server error when download fails")
    void shouldReturnInternalServerErrorWhenDownloadFails() {
        Long attachmentId = 1L;

        AttachmentDTO attachment = AttachmentDTO.builder()
                .attachmentId(attachmentId)
                .fileName("document.pdf")
                .fileType("application/pdf")
                .cloudinaryUrl("invalid-url")
                .build();

        when(attachmentService.getAttachmentById(attachmentId))
                .thenReturn(attachment);

        ResponseEntity<Resource> response =
                attachmentController.downloadAttachment(attachmentId);

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode()
        );

        assertNull(response.getBody());

        verify(attachmentService).getAttachmentById(attachmentId);
    }
}
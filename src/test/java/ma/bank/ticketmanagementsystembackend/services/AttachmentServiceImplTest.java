package ma.bank.ticketmanagementsystembackend.services;

import com.cloudinary.Cloudinary;
import ma.bank.ticketmanagementsystembackend.cloudinary.CloudinaryService;
import ma.bank.ticketmanagementsystembackend.dtos.dto.AttachmentDTO;
import ma.bank.ticketmanagementsystembackend.entities.AppUser;
import ma.bank.ticketmanagementsystembackend.entities.Attachment;
import ma.bank.ticketmanagementsystembackend.entities.Ticket;
import ma.bank.ticketmanagementsystembackend.entities.Role;
import ma.bank.ticketmanagementsystembackend.mappers.AttachmentMapper;
import ma.bank.ticketmanagementsystembackend.repositories.AttachmentRepository;
import ma.bank.ticketmanagementsystembackend.repositories.TicketRepository;
import ma.bank.ticketmanagementsystembackend.exceptions.BusinessException;
import ma.bank.ticketmanagementsystembackend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceImplTest {
    @Mock
    private AttachmentRepository attachmentRepository;
    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private CloudinaryService cloudinaryService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private Cloudinary cloudinary;
    @Mock
    private AttachmentMapper attachmentMapper;
    @InjectMocks
    private AttachmentServiceImpl attachmentService;

    private Ticket ticket;
    private AppUser appUser;
    private Attachment attachment;
    private AttachmentDTO attachmentDTO;

    @BeforeEach
    void setUp() {

        ticket = new Ticket();
        ticket.setTicketId(1L);

        appUser = new AppUser();
        appUser.setUserId(99L);
        appUser.setEmail("owner@bank.ma");
        appUser.setRoles(Set.of(Role.USER));
        ticket.setCreatedBy(appUser);

        attachment = Attachment.builder()
                .attachmentId(1L)
                .fileName("file.txt")
                .fileType("text/plain")
                .fileSize(100L)
                .cloudinaryUrl("https://res.cloudinary.com/" +
                        "dtjl2h2ph/raw/upload/v1773701060/ticket-attachments/vqzrbyrrvbkuialjk1os")
                .cloudinaryPublicId("publicId")
                .ticket(ticket)
                .build();

        attachmentDTO = new AttachmentDTO();
        attachmentDTO.setAttachmentId(1L);
    }

    @Nested
    class UploadAttachmentTest {
        @DisplayName("Should upload attachment successfully")
        @Test
        void shouldUploadAttachmentSuccessfully() throws IOException {

            MultipartFile file = new MockMultipartFile(
                    "file",
                    "file.png",
                    "image/png",
                    new byte[]{
                            (byte) 0x89, 0x50, 0x4E, 0x47,
                            0x0D, 0x0A, 0x1A, 0x0A,
                            0x00, 0x00, 0x00, 0x0D,
                            0x49, 0x48, 0x44, 0x52
                    }
            );

            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

            Map<String, String> uploadResult = new HashMap<>();
            uploadResult.put("url", "http://test.com/file.txt");
            uploadResult.put("publicId", "publicId");

            when(cloudinaryService.uploadFile(file)).thenReturn(uploadResult);
            when(attachmentRepository.save(any())).thenReturn(attachment);
            when(attachmentMapper.toDTO(any())).thenReturn(attachmentDTO);

            AttachmentDTO result = attachmentService.
                    uploadAttachment(1L, file);

            assertNotNull(result);
            assertEquals(1L, result.getAttachmentId());

            verify(cloudinaryService).uploadFile(file);
            verify(attachmentRepository).save(any());
            verify(attachmentMapper).toDTO(any());
        }

        @DisplayName("Should throw exception when ticket not found")
        @Test
        void shouldThrowExceptionWhenTicketNotFound() {

            MultipartFile file = mock(MultipartFile.class);

            when(ticketRepository.findById(1L)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class, () -> {
                attachmentService.uploadAttachment(1L, file);
            });

            assertEquals("ticket not found", ex.getMessage());
        }
    }

    @Nested
    class GetAttachmentsByTicketTest {
        @DisplayName("Should get attachments by ticket successfully")
        @Test
        void shouldReturnAttachmentsByTicket() {

            when(attachmentRepository.findByTicket_TicketId(1L))
                    .thenReturn(List.of(attachment));

            when(attachmentMapper.toDTO(attachment)).thenReturn(attachmentDTO);

            List<AttachmentDTO> result = attachmentService.getAttachmentsByTicket(1L);

            assertEquals(1, result.size());
            verify(attachmentMapper).toDTO(attachment);
        }
    }

    @Nested
    class DeleteAttachmentTest {
        @DisplayName("Should delete attachment successfully")
        @Test
        void shouldDeleteAttachment() throws IOException {

            when(attachmentRepository.findById(1L)).thenReturn(Optional.of(attachment));
            when(userRepository.findByEmail("owner@bank.ma")).thenReturn(Optional.of(appUser));

            attachmentService.deleteAttachment(1L,appUser);

            verify(cloudinaryService).deleteFile("publicId");
            verify(attachmentRepository).delete(attachment);
        }

        @DisplayName("Should throw exception when user cannot delete attachment")
        @Test
        void shouldThrowExceptionWhenUserIsNotAllowed() throws IOException {

            AppUser otherUser = new AppUser();
            otherUser.setEmail("other@bank.ma");
            otherUser.setRoles(Set.of(Role.USER));

            when(attachmentRepository.findById(1L)).thenReturn(Optional.of(attachment));
            when(userRepository.findByEmail("other@bank.ma")).thenReturn(Optional.of(otherUser));

            BusinessException ex = assertThrows(BusinessException.class, () -> {
                attachmentService.deleteAttachment(1L, otherUser);
            });

            assertEquals("You don't have permission to delete this attachment", ex.getMessage());
            verify(attachmentRepository, never()).delete(any());
            verify(cloudinaryService, never()).deleteFile(any());
        }

        @DisplayName("Should throw exception when attachment not found")
        @Test
        void shouldThrowExceptionWhenDeletingNotFound() {

            when(attachmentRepository.findById(1L)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class, () -> {
                attachmentService.deleteAttachment(1L,appUser);
            });

            assertEquals("Attachment not found", ex.getMessage());
        }
    }

    @Nested
    class DownloadAttachmentTest{
        @DisplayName("Should download attachment successfully")
        @Test
        void shouldDownloadAttachment() throws IOException {

            when(attachmentRepository.findById(1L)).thenReturn(Optional.of(attachment));

            var result = attachmentService.downloadAttachment(1L);

            assertNotNull(result);
            assertEquals("file.txt", result.getFileName());
            assertEquals("text/plain", result.getContentType());
        }

        @DisplayName("Should throw exception when attachment not found")
        @Test
        void shouldThrowExceptionWhenDownloadingNotFound() {

            when(attachmentRepository.findById(1L)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class, () -> {
                attachmentService.downloadAttachment(1L);
            });

            assertEquals("Attachment not found", ex.getMessage());
        }
    }

    @Nested
    class ShouldGetAttachmentByIdTest{
        @DisplayName("Should get Attachment by id successfully")
        @Test
        void shouldReturnAttachmentById() {

            when(attachmentRepository.findById(1L)).thenReturn(Optional.of(attachment));
            when(attachmentMapper.toDTO(attachment)).thenReturn(attachmentDTO);

            AttachmentDTO result = attachmentService.getAttachmentById(1L);

            assertNotNull(result);
        }

        @DisplayName("Should throw exception when attachment not found")
        @Test
        void shouldThrowExceptionWhenGetByIdNotFound() {

            when(attachmentRepository.findById(1L)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class, () -> {
                attachmentService.getAttachmentById(1L);
            });

            assertEquals("Attachment not found", ex.getMessage());
        }
    }
}
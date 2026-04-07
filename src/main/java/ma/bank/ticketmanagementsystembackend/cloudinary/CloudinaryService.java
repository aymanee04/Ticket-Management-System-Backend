package ma.bank.ticketmanagementsystembackend.cloudinary;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface CloudinaryService {
    Map<String, String> uploadFile(MultipartFile file) throws IOException;
    void deleteFile(String publicId) throws IOException;
}

package ma.bank.ticketmanagementsystembackend.cloudinary;

import com.cloudinary.Cloudinary;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
@Service
@AllArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {
    private final Cloudinary cloudinary;

    @Override
    public Map<String, String> uploadFile(MultipartFile file) throws IOException {

        Map<String, Object> options = new HashMap<>();
        options.put("resource_type", "raw");
        options.put("folder", "ticket-attachments");
        Map uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                options
        );
        Map<String, String> result = new HashMap<>();
        result.put("url", (String) uploadResult.get("secure_url"));
        result.put("publicId", (String) uploadResult.get("public_id"));

        return result;
    }

    @Override
    public void deleteFile(String publicId) throws IOException {
        cloudinary.uploader().destroy(
                publicId,
                Map.of("resource_type", "raw")
        );
    }
}

package ma.bank.ticketmanagementsystembackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DownloadedFile {
    private byte[] data;
    private String fileName;
    private String contentType;
}

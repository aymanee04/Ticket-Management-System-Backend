package ma.bank.ticketmanagementsystembackend.exceptions;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record ErrorResponse(
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String type
) {
    public static ErrorResponse of(int status, String error, String message, String type) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, type);
    }
}

package ma.bank.ticketmanagementsystembackend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateTicketRequest {
    @NotBlank
    @Size(max = 100)
    private String title;
    @NotBlank
    private String description;
    private Long createdById;
}

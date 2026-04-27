package ma.bank.ticketmanagementsystembackend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class CreateIncidentRequest {
    @NotBlank
    @Size(max = 100)
    private String title;
    @NotBlank
    private String description;
    private Long createdById;
    private Set<Long> clientIds;
}

package ma.bank.ticketmanagementsystembackend.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignTicketRequest {
    @NotNull
    private Long assignedToId;
}

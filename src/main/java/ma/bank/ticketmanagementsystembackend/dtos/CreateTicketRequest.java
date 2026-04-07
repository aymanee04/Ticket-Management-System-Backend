package ma.bank.ticketmanagementsystembackend.dtos;

import lombok.Data;

import java.util.Set;
@Data
public class CreateTicketRequest {
    private String title;
    private String description;
    private Long createdById;
}

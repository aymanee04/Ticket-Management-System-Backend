package ma.bank.ticketmanagementsystembackend.dtos;

import lombok.Data;

import java.util.Set;

@Data
public class CreateIncidentRequest {
    private String title;
    private String description;
    private Long createdById;
    private Set<Long> clientIds;
}

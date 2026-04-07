package ma.bank.ticketmanagementsystembackend.dtos;

import lombok.Data;

@Data
public class ValidateTicketRequest {
    private Long adminId;
    private String comment;
}

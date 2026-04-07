package ma.bank.ticketmanagementsystembackend.dtos;

import lombok.Data;
import ma.bank.ticketmanagementsystembackend.entities.ClientStatus;

import java.time.LocalDateTime;
@Data
public class UpdateClientRequest {
    private String name;
    private String email;
    private String phone;
    private String company;
    private ClientStatus status;
    private LocalDateTime createdAt;
}

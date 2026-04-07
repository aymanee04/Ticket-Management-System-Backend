package ma.bank.ticketmanagementsystembackend.dtos.dto;

import lombok.*;
import ma.bank.ticketmanagementsystembackend.entities.ClientStatus;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientDTO {
    private Long clientId;
    private String name;
    private String email;
    private String phone;
    private String company;
    private ClientStatus status;
    private String suspensionReason;
    private LocalDateTime suspendedAt;
    private Long suspendedBy;
    private LocalDateTime createdAt;
    private Set<Long> ticketIds;
    private Set<AppUserDTO> employees;
}
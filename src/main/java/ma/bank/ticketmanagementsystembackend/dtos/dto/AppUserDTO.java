package ma.bank.ticketmanagementsystembackend.dtos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.bank.ticketmanagementsystembackend.entities.Role;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUserDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Long userId;
    private String name;
    private String jobTitle;
    private String email;
    private Collection<Role> roles;
    private Long clientId;
    private String clientName;
}
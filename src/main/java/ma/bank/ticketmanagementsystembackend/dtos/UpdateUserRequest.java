package ma.bank.ticketmanagementsystembackend.dtos;

import lombok.Data;
import ma.bank.ticketmanagementsystembackend.entities.Role;

import java.util.Collection;
@Data
public class UpdateUserRequest {
    private String name;
    private String email;
    private String jobTitle;
    private String password;
    private Collection<Role> roles;
    private Long clientId;
}

package ma.bank.ticketmanagementsystembackend.dtos;

import lombok.Builder;
import lombok.Data;
import ma.bank.ticketmanagementsystembackend.entities.Role;

import java.util.Collection;

@Data
public class CreateUserRequest {
    private String name;
    private String email;
    private String jobTitle;
    private String password;
    private Collection<Role> roles;
    private Long clientId;
}

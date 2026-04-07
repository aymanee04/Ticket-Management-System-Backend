package ma.bank.ticketmanagementsystembackend.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "clients")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"tickets", "employees"})
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long clientId;

    private String name;

    private String email;

    private String phone;

    private String company;

    @Enumerated(EnumType.STRING)
    private ClientStatus status;

    private String suspensionReason;
    private LocalDateTime suspendedAt;
    private Long suspendedBy;

    private LocalDateTime createdAt;

    @ManyToMany(mappedBy = "clients")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Set<Ticket> tickets;

    @OneToMany(mappedBy = "client", fetch = FetchType.EAGER)
    private Set<AppUser> employees;
}
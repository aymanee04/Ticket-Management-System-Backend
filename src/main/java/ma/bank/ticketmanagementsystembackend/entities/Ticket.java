package ma.bank.ticketmanagementsystembackend.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
@Table(name = "tickets")
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "clients")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long ticketId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private TicketStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime validatedAt;

    private String validationComment;


    @ManyToOne
    @JoinColumn(name = "created_by")
    private AppUser createdBy;


    @ManyToOne
    @JoinColumn(name = "assigned_to")
    private AppUser assignedTo;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attachment> attachments;


    @ManyToOne
    @JoinColumn(name = "validated_by")
    private AppUser validatedBy;

    @ManyToMany
    @JoinTable(
            name = "ticket_client",
            joinColumns = @JoinColumn(name = "ticket_id"),
            inverseJoinColumns = @JoinColumn(name = "client_id")
    )
    @JsonProperty(access =  JsonProperty.Access.WRITE_ONLY)
    private Set<Client> clients;
}

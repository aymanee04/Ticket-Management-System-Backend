package ma.bank.ticketmanagementsystembackend.entities;

import java.io.Serial;
import java.io.Serializable;

public enum TicketStatus implements Serializable {
    IN_PROGRESS,
    VALIDATED,
    REJECTED,
    ARCHIVED,
    CANCELLED,
    PENDING_VALIDATION;
    
    @Serial
    private static final long serialVersionUID = 1L;
}

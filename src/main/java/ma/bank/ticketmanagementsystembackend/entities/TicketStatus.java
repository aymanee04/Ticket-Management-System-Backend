package ma.bank.ticketmanagementsystembackend.entities;

import java.io.Serializable;

public enum TicketStatus implements Serializable {
    IN_PROGRESS,
    VALIDATED,
    REJECTED,
    ARCHIVED,
    CANCELLED,
    PENDING_VALIDATION
}

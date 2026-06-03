package ma.bank.ticketmanagementsystembackend.entities;

import java.io.Serial;
import java.io.Serializable;

public enum ClientStatus implements Serializable {
    ACTIVE,
    INACTIVE,
    SUSPENDED;
    @Serial
    private static final long serialVersionUID = 1L;
}

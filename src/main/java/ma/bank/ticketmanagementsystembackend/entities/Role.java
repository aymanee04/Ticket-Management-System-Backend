package ma.bank.ticketmanagementsystembackend.entities;

import java.io.Serial;
import java.io.Serializable;

public enum Role implements Serializable {
    USER, ADMIN, MANAGER;
    
    @Serial
    private static final long serialVersionUID = 1L;
}

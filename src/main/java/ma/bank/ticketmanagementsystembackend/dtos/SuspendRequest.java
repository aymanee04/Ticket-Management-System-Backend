package ma.bank.ticketmanagementsystembackend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SuspendRequest(

        @NotBlank(message = "Suspension reason is required")
        @Size(min = 10, message = "Suspension reason must be at least 10 characters")
        String suspensionReason
) {}

package ma.bank.ticketmanagementsystembackend.dtos;

import jakarta.validation.constraints.NotBlank;

public class TicketActionRequest {

    @NotBlank(message = "Comment is required")
    private String comment;

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}

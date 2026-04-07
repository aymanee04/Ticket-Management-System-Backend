package ma.bank.ticketmanagementsystembackend.services;

import ma.bank.ticketmanagementsystembackend.entities.AppUser;
import ma.bank.ticketmanagementsystembackend.entities.Client;
import ma.bank.ticketmanagementsystembackend.entities.Ticket;

public interface EmailService {

    void sendTicketCreatedEmail(Ticket ticket);
    void sendTicketAssignedEmail(Ticket ticket, AppUser assignedTo);
    void sendTicketValidatedEmail(Ticket ticket);
    void sendTicketRejectedEmail(Ticket ticket);

    void sendClientSuspendedEmail(Client client);
    void sendClientReactivatedEmail(Client client);

    void sendPasswordChangedEmail(AppUser user);
    void sendWelcomeEmail(AppUser user, String tempPassword);

    void sendIncidentNotification(Ticket incident);
}
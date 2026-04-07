package ma.bank.ticketmanagementsystembackend.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import ma.bank.ticketmanagementsystembackend.entities.AppUser;
import ma.bank.ticketmanagementsystembackend.entities.Client;
import ma.bank.ticketmanagementsystembackend.entities.Ticket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.email.from:noreply@ticketsystem.com}")
    private String fromEmail;

    @Value("${app.email.enabled:true}")
    private Boolean emailEnabled;

    @Value("${app.base.url:http://localhost:4200}")
    private String baseUrl;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm");

    @Override
    @Async
    public void sendTicketCreatedEmail(Ticket ticket) {
        if (emailEnabled == null || !emailEnabled) {
            System.out.println("Email sending is disabled");
            return;
        }

        try {
            Context context = new Context(Locale.ENGLISH);
            context.setVariable("userName", ticket.getCreatedBy().getName());
            context.setVariable("ticketId", ticket.getTicketId());
            context.setVariable("title", ticket.getTitle());
            context.setVariable("description", ticket.getDescription());
            context.setVariable("status", ticket.getStatus().toString());
            context.setVariable("createdAt", ticket.getCreatedAt().format(DATE_FORMATTER));
            context.setVariable("ticketUrl", baseUrl + "/tickets/" + ticket.getTicketId());

            String htmlContent = templateEngine.process("emails/ticket-created", context);

            sendHtmlEmail(
                    ticket.getCreatedBy().getEmail(),
                    "Ticket #" + ticket.getTicketId() + " Created - " + ticket.getTitle(),
                    htmlContent
            );

        } catch (Exception e) {
            System.err.println("Failed to send ticket created email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    @Async
    public void sendTicketAssignedEmail(Ticket ticket, AppUser assignedTo) {
        if (emailEnabled == null || !emailEnabled) {
            System.out.println("Email sending is disabled");
            return;
        }

        try {
            Context context = new Context(Locale.ENGLISH);
            context.setVariable("managerName", assignedTo.getName());
            context.setVariable("ticketId", ticket.getTicketId());
            context.setVariable("title", ticket.getTitle());
            context.setVariable("description", ticket.getDescription());
            context.setVariable("status", ticket.getStatus().toString());
            context.setVariable("createdBy", ticket.getCreatedBy().getName());
            context.setVariable("assignedAt", ticket.getUpdatedAt().format(DATE_FORMATTER));
            context.setVariable("ticketUrl", baseUrl + "/tickets/" + ticket.getTicketId());

            String htmlContent = templateEngine.process("emails/ticket-assigned", context);

            sendHtmlEmail(
                    assignedTo.getEmail(),
                    "New Ticket Assigned to You - #" + ticket.getTicketId(),
                    htmlContent
            );

        } catch (Exception e) {
            System.err.println("Failed to send ticket assigned email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    @Async
    public void sendTicketValidatedEmail(Ticket ticket) {
        if (emailEnabled == null || !emailEnabled) {
            System.out.println("Email sending is disabled");
            return;
        }

        try {
            Context context = new Context(Locale.ENGLISH);
            context.setVariable("userName", ticket.getCreatedBy().getName());
            context.setVariable("ticketId", ticket.getTicketId());
            context.setVariable("title", ticket.getTitle());
            context.setVariable("validatedBy",
                    ticket.getValidatedBy() != null ? ticket.getValidatedBy().getName() : "System");
            context.setVariable("validatedAt", ticket.getValidatedAt().format(DATE_FORMATTER));
            context.setVariable("comment",
                    ticket.getValidationComment() != null ? ticket.getValidationComment() : "No comment provided");
            context.setVariable("ticketUrl", baseUrl + "/tickets/" + ticket.getTicketId());

            String htmlContent = templateEngine.process("emails/ticket-validated", context);

            sendHtmlEmail(
                    ticket.getCreatedBy().getEmail(),
                    "Ticket #" + ticket.getTicketId() + " Validated",
                    htmlContent
            );

            // Notify all client users
            ticket.getClients().forEach(client -> {
                if (client.getEmployees() != null) {
                    client.getEmployees().forEach(user -> {
                        if (!user.getEmail().equals(ticket.getCreatedBy().getEmail())) {
                            sendHtmlEmail(user.getEmail(),
                                    "Ticket #" + ticket.getTicketId() + " Validated",
                                    htmlContent);
                        }
                    });
                }
            });

        } catch (Exception e) {
            System.err.println("Failed to send ticket validated email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    @Async
    public void sendTicketRejectedEmail(Ticket ticket) {
        if (emailEnabled == null || !emailEnabled) {
            System.out.println("Email sending is disabled");
            return;
        }

        try {
            Context context = new Context(Locale.ENGLISH);
            context.setVariable("userName", ticket.getCreatedBy().getName());
            context.setVariable("ticketId", ticket.getTicketId());
            context.setVariable("title", ticket.getTitle());
            context.setVariable("rejectedBy",
                    ticket.getValidatedBy() != null ? ticket.getValidatedBy().getName() : "System");
            context.setVariable("rejectedAt", ticket.getValidatedAt().format(DATE_FORMATTER));
            context.setVariable("reason",
                    ticket.getValidationComment() != null ? ticket.getValidationComment() : "No reason provided");
            context.setVariable("ticketUrl", baseUrl + "/tickets/" + ticket.getTicketId());

            String htmlContent = templateEngine.process("emails/ticket-rejected", context);

            sendHtmlEmail(
                    ticket.getCreatedBy().getEmail(),
                    "Ticket #" + ticket.getTicketId() + " Rejected",
                    htmlContent
            );

        } catch (Exception e) {
            System.err.println("Failed to send ticket rejected email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    @Async
    public void sendClientSuspendedEmail(Client client) {
        if (emailEnabled == null || !emailEnabled) {
            System.out.println("⚠️ Email sending is disabled");
            return;
        }

        try {
            Context context = new Context(Locale.ENGLISH);
            context.setVariable("companyName", client.getCompany());
            context.setVariable("clientName", client.getName());
            context.setVariable("suspendedAt", client.getSuspendedAt().format(DATE_FORMATTER));
            context.setVariable("reason",
                    client.getSuspensionReason() != null ? client.getSuspensionReason() : "No specific reason provided");

            String htmlContent = templateEngine.process("emails/client-suspended", context);

            if (client.getEmployees() != null) {
                client.getEmployees().forEach(user -> {
                    sendHtmlEmail(
                            user.getEmail(),
                            "URGENT: Account Suspended - Action Required",
                            htmlContent
                    );
                });
            }

        } catch (Exception e) {
            System.err.println("Failed to send client suspended email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    @Async
    public void sendClientReactivatedEmail(Client client) {
        if (emailEnabled == null || !emailEnabled) {
            System.out.println("Email sending is disabled");
            return;
        }

        try {
            Context context = new Context(Locale.ENGLISH);
            context.setVariable("companyName", client.getCompany());
            context.setVariable("clientName", client.getName());
            context.setVariable("loginUrl", baseUrl + "/login");

            String htmlContent = templateEngine.process("emails/client-reactivated", context);

            if (client.getEmployees() != null) {
                client.getEmployees().forEach(user -> {
                    sendHtmlEmail(
                            user.getEmail(),
                            "Account Reactivated - Welcome Back!",
                            htmlContent
                    );
                });
            }

        } catch (Exception e) {
            System.err.println("Failed to send client reactivated email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    @Async
    public void sendPasswordChangedEmail(AppUser user) {
        if (emailEnabled == null || !emailEnabled) {
            System.out.println("Email sending is disabled");
            return;
        }

        try {
            Context context = new Context(Locale.ENGLISH);
            context.setVariable("userName", user.getName());
            context.setVariable("changedAt",
                    java.time.LocalDateTime.now().format(DATE_FORMATTER));

            String htmlContent = templateEngine.process("emails/password-changed", context);

            sendHtmlEmail(
                    user.getEmail(),
                    "Password Changed Successfully",
                    htmlContent
            );

        } catch (Exception e) {
            System.err.println("Failed to send password changed email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    @Async
    public void sendWelcomeEmail(AppUser user, String tempPassword) {
        if (emailEnabled == null || !emailEnabled) {
            System.out.println("Email sending is disabled");
            return;
        }

        try {
            Context context = new Context(Locale.ENGLISH);
            context.setVariable("userName", user.getName());
            context.setVariable("email", user.getEmail());
            context.setVariable("tempPassword", tempPassword);
            context.setVariable("role", user.getRoles().toString());
            context.setVariable("company",
                    user.getClient() != null ? user.getClient().getName() : "N/A (Admin)");
            context.setVariable("loginUrl", baseUrl + "/login");

            String htmlContent = templateEngine.process("emails/welcome-user", context);

            sendHtmlEmail(
                    user.getEmail(),
                    "Welcome to Ticket Management System!",
                    htmlContent
            );

        } catch (Exception e) {
            System.err.println("Failed to send welcome email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    @Async
    public void sendIncidentNotification(Ticket incident) {
        if (emailEnabled == null || !emailEnabled) {
            System.out.println("Email sending is disabled");
            return;
        }

        try {
            Context context = new Context(Locale.ENGLISH);
            context.setVariable("incidentId", incident.getTicketId());
            context.setVariable("title", incident.getTitle());
            context.setVariable("description", incident.getDescription());
            context.setVariable("reportedBy", incident.getCreatedBy().getName());
            context.setVariable("reportedAt", incident.getCreatedAt().format(DATE_FORMATTER));
            context.setVariable("status", incident.getStatus().toString());
            context.setVariable("ticketUrl", baseUrl + "/tickets/" + incident.getTicketId());

            String htmlContent = templateEngine.process("emails/incident-notification", context);

            incident.getClients().forEach(client -> {
                if (client.getEmployees() != null) {
                    client.getEmployees().forEach(user -> {
                        sendHtmlEmail(
                                user.getEmail(),
                                "SYSTEM INCIDENT: " + incident.getTitle(),
                                htmlContent
                        );
                    });
                }
            });

        } catch (Exception e) {
            System.err.println("Failed to send incident notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("Email sent successfully to: " + to);

        } catch (MessagingException e) {
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
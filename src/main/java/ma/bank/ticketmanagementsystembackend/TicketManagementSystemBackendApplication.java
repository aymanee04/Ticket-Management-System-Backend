package ma.bank.ticketmanagementsystembackend;

import ma.bank.ticketmanagementsystembackend.dtos.dto.AppUserDTO;
import ma.bank.ticketmanagementsystembackend.dtos.dto.ClientDTO;
import ma.bank.ticketmanagementsystembackend.dtos.dto.TicketDTO;
import ma.bank.ticketmanagementsystembackend.entities.*;
import ma.bank.ticketmanagementsystembackend.services.ClientService;
import ma.bank.ticketmanagementsystembackend.services.TicketService;
import ma.bank.ticketmanagementsystembackend.services.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@EnableAsync
public class TicketManagementSystemBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketManagementSystemBackendApplication.class, args);
    }

//    @Bean
//    CommandLineRunner commandLineRunner(
//            ClientService clientService,
//            UserService userService,
//            TicketService ticketService) {
//
//        return args -> {
//            if (userService.getAllUsers().isEmpty()) {
//
//                // ========== CLIENTS ==========
//                ClientDTO clientA = clientService.createClient(
//                        Client.builder()
//                                .name("Bank of Morocco")
//                                .email("contact@bankommm.com")
//                                .phone("123456789")
//                                .company("Bank of Morocco")
//                                .status(ClientStatus.ACTIVE)
//                                .build()
//                );
//
//                ClientDTO clientB = clientService.createClient(
//                        Client.builder()
//                                .name("Atlas Telecom")
//                                .email("contact@atlasTeleccc.com")
//                                .phone("987654321")
//                                .company("Atlas Telecom")
//                                .status(ClientStatus.ACTIVE)
//                                .build()
//                );
//
//                ClientDTO clientC = clientService.createClient(
//                        Client.builder()
//                                .name("Binarios")
//                                .email("contact@binariosss.com")
//                                .phone("555666777")
//                                .company("Binarios Technology")
//                                .status(ClientStatus.ACTIVE)
//                                .build()
//                );
//
//                ClientDTO clientD = clientService.createClient(
//                        Client.builder()
//                                .name("Medina Tech")
//                                .email("info@medinatechhh.ma")
//                                .phone("212555123456")
//                                .company("Medina Tech Solutions")
//                                .status(ClientStatus.ACTIVE)
//                                .build()
//                );
//
//                ClientDTO clientE = clientService.createClient(
//                        Client.builder()
//                                .name("Sahara Logistics")
//                                .email("support@saharaloggg.com")
//                                .phone("212666789012")
//                                .company("Sahara Logistics Co.")
//                                .status(ClientStatus.ACTIVE)
//                                .build()
//                );
//
//                // ========== ADMINS ==========
//                AppUserDTO aymane = userService.createUser(
//                        AppUser.builder()
//                                .name("Aymane Jemmaa")
//                                .email("aymane@gmail.com")
//                                .jobTitle("Senior Backend Developer")
//                                .password("admin123")
//                                .roles(new ArrayList<>(List.of(Role.ADMIN)))
//                                .client(null)
//                                .build()
//                );
//
//                AppUserDTO adam = userService.createUser(
//                        AppUser.builder()
//                                .name("Adam Tayan")
//                                .email("agent@gmail.com")
//                                .jobTitle("Database Administrator")
//                                .password("admin123")
//                                .roles(new ArrayList<>(List.of(Role.ADMIN)))
//                                .client(null)
//                                .build()
//                );
//
//                AppUserDTO sarah = userService.createUser(
//                        AppUser.builder()
//                                .name("Sarah Martinez")
//                                .email("sarah@gmail.com")
//                                .jobTitle("System Architect")
//                                .password("admin123")
//                                .roles(new ArrayList<>(List.of(Role.ADMIN)))
//                                .client(null)
//                                .build()
//                );
//
//                // ========== MANAGERS ==========
//                AppUserDTO manager1 = userService.createUser(
//                        AppUser.builder()
//                                .name("Emily Stone")
//                                .email("emma@gmail.com")
//                                .jobTitle("System Administrator")
//                                .password("admin123")
//                                .roles(new ArrayList<>(List.of(Role.USER, Role.MANAGER)))
//                                .client(Client.builder()
//                                        .clientId(clientC.getClientId())
//                                        .build())
//                                .build()
//                );
//
//                AppUserDTO manager2 = userService.createUser(
//                        AppUser.builder()
//                                .name("Ryan Gosling")
//                                .email("ryan@gmail.com")
//                                .jobTitle("Cloud Engineer")
//                                .password("admin123")
//                                .roles(new ArrayList<>(List.of(Role.MANAGER)))
//                                .client(Client.builder()
//                                        .clientId(clientB.getClientId())
//                                        .build())
//                                .build()
//                );
//
//                AppUserDTO manager3 = userService.createUser(
//                        AppUser.builder()
//                                .name("Mia Goth")
//                                .email("mia@gmail.com")
//                                .jobTitle("Data Engineer")
//                                .password("admin123")
//                                .roles(new ArrayList<>(List.of(Role.MANAGER)))
//                                .client(Client.builder()
//                                        .clientId(clientA.getClientId())
//                                        .build())
//                                .build()
//                );
//
//                AppUserDTO manager4 = userService.createUser(
//                        AppUser.builder()
//                                .name("Omar Hassan")
//                                .email("omar@gmail.com")
//                                .jobTitle("IT Manager")
//                                .password("admin123")
//                                .roles(new ArrayList<>(List.of(Role.MANAGER)))
//                                .client(Client.builder()
//                                        .clientId(clientD.getClientId())
//                                        .build())
//                                .build()
//                );
//
//                AppUserDTO manager5 = userService.createUser(
//                        AppUser.builder()
//                                .name("Fatima Zahra")
//                                .email("fatima@gmail.com")
//                                .jobTitle("Operations Manager")
//                                .password("admin123")
//                                .roles(new ArrayList<>(List.of(Role.MANAGER)))
//                                .client(Client.builder()
//                                        .clientId(clientE.getClientId())
//                                        .build())
//                                .build()
//                );
//
//                // ========== USERS - Bank of Morocco ==========
//                AppUserDTO john = userService.createUser(
//                        AppUser.builder()
//                                .name("John Doe")
//                                .email("john@gmail.com")
//                                .jobTitle("Frontend Developer")
//                                .password("admin123")
//                                .roles(new ArrayList<>(List.of(Role.USER)))
//                                .client(Client.builder()
//                                        .clientId(clientA.getClientId())
//                                        .build())
//                                .build()
//                );
//
//                AppUserDTO karim = userService.createUser(
//                        AppUser.builder()
//                                .name("Karim Benzema")
//                                .email("karim@gmail.com")
//                                .jobTitle("Financial Analyst")
//                                .password("admin123")
//                                .roles(new ArrayList<>(List.of(Role.USER)))
//                                .client(Client.builder()
//                                        .clientId(clientA.getClientId())
//                                        .build())
//                                .build()
//                );
//
//                AppUserDTO amina = userService.createUser(
//                        AppUser.builder()
//                                .name("Amina El Mansouri")
//                                .email("amina@gmail.com")
//                                .jobTitle("Business Analyst")
//                                .password("admin123")
//                                .roles(new ArrayList<>(List.of(Role.USER)))
//                                .client(Client.builder()
//                                        .clientId(clientA.getClientId())
//                                        .build())
//                                .build()
//                );
//
//                // ========== USERS - Atlas Telecom ==========
//                AppUserDTO jane = userService.createUser(
//                        AppUser.builder()
//                                .name("Jane Smith")
//                                .email("ajemmaa0@gmail.com")
//                                .jobTitle("HR Manager")
//                                .password("admin123")
//                                .roles(new ArrayList<>(List.of(Role.USER)))
//                                .client(Client.builder()
//                                        .clientId(clientB.getClientId())
//                                        .build())
//                                .build()
//                );
//
//                AppUserDTO youssef = userService.createUser(
//                        AppUser.builder()
//                                .name("Youssef Tahiri")
//                                .email("youssef@gmail.com")
//                                .jobTitle("Network Engineer")
//                                .password("admin123")
//                                .roles(new ArrayList<>(List.of(Role.USER)))
//                                .client(Client.builder()
//                                        .clientId(clientB.getClientId())
//                                        .build())
//                                .build()
//                );
//
//                AppUserDTO laila = userService.createUser(
//                        AppUser.builder()
//                                .name("Laila Bennani")
//                                .email("laila@gmail.com")
//                                .jobTitle("Customer Success Manager")
//                                .password("admin123")
//                                .roles(new ArrayList<>(List.of(Role.USER)))
//                                .client(Client.builder()
//                                        .clientId(clientB.getClientId())
//                                        .build())
//                                .build()
//                );
//
//                // ========== USERS - Binarios ==========
//                AppUserDTO mike = userService.createUser(
//                        AppUser.builder()
//                                .name("Mike Johnson")
//                                .email("mike@gmail.com")
//                                .jobTitle("Network Administrator")
//                                .password("admin123")
//                                .roles(new ArrayList<>(List.of(Role.USER)))
//                                .client(Client.builder()
//                                        .clientId(clientC.getClientId())
//                                        .build())
//                                .build()
//                );
//
//                AppUserDTO sofia = userService.createUser(
//                        AppUser.builder()
//                                .name("Sofia Alami")
//                                .email("sofia@gmail.com")
//                                .jobTitle("UX Designer")
//                                .password("admin123")
//                                .roles(new ArrayList<>(List.of(Role.USER)))
//                                .client(Client.builder()
//                                        .clientId(clientC.getClientId())
//                                        .build())
//                                .build()
//                );
//
//                AppUserDTO mehdi = userService.createUser(
//                        AppUser.builder()
//                                .name("Mehdi Fassi")
//                                .email("mehdi@gmail.com")
//                                .jobTitle("DevOps Engineer")
//                                .password("admin123")
//                                .roles(new ArrayList<>(List.of(Role.USER)))
//                                .client(Client.builder()
//                                        .clientId(clientC.getClientId())
//                                        .build())
//                                .build()
//                );
//
//                // ========== USERS - Medina Tech ==========
//                AppUserDTO hassan = userService.createUser(
//                        AppUser.builder()
//                                .name("Hassan Idrissi")
//                                .email("hassan@gmail.com")
//                                .jobTitle("Full Stack Developer")
//                                .password("admin123")
//                                .roles(new ArrayList<>(List.of(Role.USER)))
//                                .client(Client.builder()
//                                        .clientId(clientD.getClientId())
//                                        .build())
//                                .build()
//                );
//
//                AppUserDTO nadia = userService.createUser(
//                        AppUser.builder()
//                                .name("Nadia Berrada")
//                                .email("nadia@gmail.com")
//                                .jobTitle("Product Manager")
//                                .password("admin123")
//                                .roles(new ArrayList<>(List.of(Role.USER)))
//                                .client(Client.builder()
//                                        .clientId(clientD.getClientId())
//                                        .build())
//                                .build()
//                );
//
//                // ========== USERS - Sahara Logistics ==========
//                AppUserDTO rachid = userService.createUser(
//                        AppUser.builder()
//                                .name("Rachid Tazi")
//                                .email("rachid@gmail.com")
//                                .jobTitle("Supply Chain Analyst")
//                                .password("admin123")
//                                .roles(new ArrayList<>(List.of(Role.USER)))
//                                .client(Client.builder()
//                                        .clientId(clientE.getClientId())
//                                        .build())
//                                .build()
//                );
//
//                AppUserDTO samira = userService.createUser(
//                        AppUser.builder()
//                                .name("Samira Chakir")
//                                .email("samira@gmail.com")
//                                .jobTitle("Logistics Coordinator")
//                                .password("admin123")
//                                .roles(new ArrayList<>(List.of(Role.USER)))
//                                .client(Client.builder()
//                                        .clientId(clientE.getClientId())
//                                        .build())
//                                .build()
//                );
//
//                // ========== TICKETS - Bank of Morocco ==========
//                TicketDTO ticket1 = ticketService.createTicket(
//                        "Login Issue - Cannot Access Banking Portal",
//                        "Multiple users reporting inability to login to the main banking portal. Error message: 'Invalid credentials' even with correct password.",
//                        john.getUserId(),
//                        (clientA.getClientId())
//                );
//
//                TicketDTO ticket2 = ticketService.createTicket(
//                        "Database Connection Timeout",
//                        "Transaction processing is experiencing intermittent timeouts. Database connection pool seems to be exhausted during peak hours.",
//                        karim.getUserId(),
//                        (clientA.getClientId())
//                );
//
//                TicketDTO ticket3 = ticketService.createTicket(
//                        "Report Generation Taking Too Long",
//                        "Monthly financial reports that used to take 5 minutes are now taking over 30 minutes to generate. Need optimization.",
//                        amina.getUserId(),
//                        (clientA.getClientId())
//                );
//
//                TicketDTO ticket4 = ticketService.createTicket(
//                        "Mobile App Sync Issue",
//                        "Customer account balances not syncing properly between web and mobile applications.",
//                        john.getUserId(),
//                        (clientA.getClientId())
//                );
//
//                // ========== TICKETS - Atlas Telecom ==========
//                TicketDTO ticket5 = ticketService.createTicket(
//                        "Payment Gateway Failure",
//                        "Payment service is down for multiple clients. Customers cannot complete transactions. URGENT!",
//                        jane.getUserId(),
//                        (clientB.getClientId())
//                );
//
//                TicketDTO ticket6 = ticketService.createTicket(
//                        "Network Bandwidth Monitoring Dashboard",
//                        "Need a real-time dashboard to monitor network bandwidth usage across all regions.",
//                        youssef.getUserId(),
//                        (clientB.getClientId())
//                );
//
//                TicketDTO ticket7 = ticketService.createTicket(
//                        "Customer Portal Performance Issues",
//                        "Customer portal is loading very slowly. Page load times exceeding 10 seconds.",
//                        laila.getUserId(),
//                        (clientB.getClientId())
//                );
//
//                TicketDTO ticket8 = ticketService.createTicket(
//                        "SMS Notification Service Down",
//                        "Customers are not receiving SMS notifications for bill payments and service updates.",
//                        jane.getUserId(),
//                        (clientB.getClientId())
//                );
//
//                // ========== TICKETS - Binarios ==========
//                TicketDTO ticket9 = ticketService.createTicket(
//                        "Dark Mode Feature Request",
//                        "Client requests dark mode feature for better user experience during night time usage.",
//                        mike.getUserId(),
//                        (clientC.getClientId())
//                );
//
//                TicketDTO ticket10 = ticketService.createTicket(
//                        "API Rate Limiting Implementation",
//                        "Need to implement rate limiting on public APIs to prevent abuse and ensure fair usage.",
//                        mehdi.getUserId(),
//                        (clientC.getClientId())
//                );
//
//                TicketDTO ticket11 = ticketService.createTicket(
//                        "UI Redesign for Admin Panel",
//                        "Current admin panel UI is outdated. Request for modern, responsive redesign.",
//                        sofia.getUserId(),
//                        (clientC.getClientId())
//                );
//
//                TicketDTO ticket12 = ticketService.createTicket(
//                        "Docker Container Memory Leak",
//                        "Production containers are experiencing memory leaks. Need investigation and fix.",
//                        mehdi.getUserId(),
//                        (clientC.getClientId())
//                );
//
//                // ========== TICKETS - Medina Tech ==========
//                TicketDTO ticket13 = ticketService.createTicket(
//                        "CI/CD Pipeline Failing",
//                        "Automated deployment pipeline is failing at the build stage. Blocking all releases.",
//                        hassan.getUserId(),
//                        (clientD.getClientId())
//                );
//
//                TicketDTO ticket14 = ticketService.createTicket(
//                        "Feature: Multi-language Support",
//                        "Need to add Arabic and French language support to the application.",
//                        nadia.getUserId(),
//                        (clientD.getClientId())
//                );
//
//                TicketDTO ticket15 = ticketService.createTicket(
//                        "Security Audit Required",
//                        "Annual security audit needs to be conducted. Request penetration testing.",
//                        hassan.getUserId(),
//                        (clientD.getClientId())
//                );
//
//                // ========== TICKETS - Sahara Logistics ==========
//                TicketDTO ticket16 = ticketService.createTicket(
//                        "Real-time Tracking Not Updating",
//                        "GPS tracking for delivery vehicles is not updating in real-time. Showing positions from 2 hours ago.",
//                        rachid.getUserId(),
//                        (clientE.getClientId())
//                );
//
//                TicketDTO ticket17 = ticketService.createTicket(
//                        "Inventory Management System Bug",
//                        "System showing incorrect stock levels. Causing overselling issues.",
//                        samira.getUserId(),
//                        (clientE.getClientId())
//                );
//
//                TicketDTO ticket18 = ticketService.createTicket(
//                        "Integration with Customs System",
//                        "Need integration with national customs system for automated import/export documentation.",
//                        rachid.getUserId(),
//                        (clientE.getClientId())
//                );
//
//
//
//                // ========== ASSIGN TICKETS TO MANAGERS ==========
//                ticketService.assignTicket(ticket1.getTicketId(), manager3.getUserId());
//                ticketService.assignTicket(ticket2.getTicketId(), manager3.getUserId());
//                ticketService.assignTicket(ticket3.getTicketId(), manager3.getUserId());
//
//                ticketService.assignTicket(ticket5.getTicketId(), manager2.getUserId());
//                ticketService.assignTicket(ticket6.getTicketId(), manager2.getUserId());
//                ticketService.assignTicket(ticket7.getTicketId(), manager2.getUserId());
//
//                ticketService.assignTicket(ticket9.getTicketId(), manager1.getUserId());
//                ticketService.assignTicket(ticket10.getTicketId(), manager1.getUserId());
//                ticketService.assignTicket(ticket11.getTicketId(), manager1.getUserId());
//
//                ticketService.assignTicket(ticket13.getTicketId(), manager4.getUserId());
//                ticketService.assignTicket(ticket14.getTicketId(), manager4.getUserId());
//
//                ticketService.assignTicket(ticket16.getTicketId(), manager5.getUserId());
//                ticketService.assignTicket(ticket17.getTicketId(), manager5.getUserId());
//
//                // ========== APPROVE TICKETS ==========
//                ticketService.approveTicket(
//                        ticket1.getTicketId(),
//                        manager3.getUserId(),
//                        "Login issue resolved. Password reset mechanism was failing due to expired security tokens. Fixed and tested."
//                );
//
//                ticketService.approveTicket(
//                        ticket5.getTicketId(),
//                        manager2.getUserId(),
//                        "Payment gateway back online. Issue was with third-party API timeout. Increased timeout limits and added retry logic."
//                );
//
//                ticketService.approveTicket(
//                        ticket10.getTicketId(),
//                        manager1.getUserId(),
//                        "Rate limiting successfully implemented. Using Redis for distributed rate limiting. Set at 100 requests per minute per IP."
//                );
//
//                ticketService.approveTicket(
//                        ticket13.getTicketId(),
//                        manager4.getUserId(),
//                        "CI/CD pipeline fixed. Missing environment variable in Jenkins configuration. All builds passing now."
//                );
//
//                // ========== REJECT TICKETS ==========
//                ticketService.rejectTicket(
//                        ticket9.getTicketId(),
//                        manager1.getUserId(),
//                        "Dark mode feature not in current roadmap. Will consider for Q3 2026 release. Requires significant UI/UX redesign effort."
//                );
//
//                ticketService.rejectTicket(
//                        ticket11.getTicketId(),
//                        manager1.getUserId(),
//                        "Admin panel redesign postponed. Current priority is on customer-facing features. Will revisit in 6 months."
//                );
//
//                ticketService.rejectTicket(
//                        ticket14.getTicketId(),
//                        manager4.getUserId(),
//                        "Multi-language support requires localization team which we don't have resources for currently. Deferred to 2027."
//                );
//
//                // ========== ARCHIVE APPROVED TICKETS ==========
//                ticketService.archiveTicket(ticket1.getTicketId());
//                ticketService.archiveTicket(ticket5.getTicketId());
//            }
//        };
//    }


//    @Bean
//    CommandLineRunner commandLineRunner(
//            ClientService clientService,
//            UserService userService,
//            TicketService ticketService) {
//
//        return args -> {
//                            ClientDTO clientA = clientService.createClient(
//                        Client.builder()
//                                .name("Bank of Morocco")
//                                .email("contact@bankommm.com")
//                                .phone("123456789")
//                                .company("Bank of Morocco")
//                                .status(ClientStatus.ACTIVE)
//                                .build()
//                );
//
//                AppUserDTO youssef = userService.createUser(
//                        AppUser.builder()
//                                .name("Youssef Tahiri")
//                                .email("ajemmaa0@gmail.com")
//                                .jobTitle("Network Engineer")
//                                .password("admin123")
//                                .roles(new ArrayList<>(List.of(Role.USER)))
//                                .client(Client.builder()
//                                        .clientId(clientA.getClientId())
//                                        .build())
//                                .build()
//                );
//        };
//    }

}
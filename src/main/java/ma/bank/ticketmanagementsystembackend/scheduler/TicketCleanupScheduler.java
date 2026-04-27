package ma.bank.ticketmanagementsystembackend.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.bank.ticketmanagementsystembackend.repositories.TicketRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

//@Component
//@RequiredArgsConstructor
//@Slf4j
public class TicketCleanupScheduler {

//    private final TicketRepository ticketRepository;
//
//    /**
//     * Runs every Friday at 23:00 (11 PM).
//     *
//     * Cron breakdown:  0  0  23  *  *  FRI
//     *                  │  │   │  │  │   └─ day of week: Friday
//     *                  │  │   │  │  └───── month: every month
//     *                  │  │   │  └──────── day of month: every day (ignored when dow set)
//     *                  │  │   └─────────── hour: 23
//     *                  │  └─────────────── minute: 0
//     *                  └────────────────── second: 0
//     *
//     * Deletes all CANCELLED tickets whose scheduledDeleteAt timestamp has passed.
//     * ARCHIVED tickets are kept indefinitely and must be hard-deleted manually by an ADMIN.
//     */
//    @Scheduled(cron = "0 0 23 * * FRI")
//    @Transactional
//    public void deleteExpiredCancelledTickets() {
//        log.info("Ticket cleanup job started at {}", LocalDateTime.now());
//
//        int deleted = ticketRepository.deleteExpiredCancelledTickets(LocalDateTime.now());
//
//        if (deleted > 0) {
//            log.info("Ticket cleanup: permanently deleted {} expired cancelled ticket(s)", deleted);
//        } else {
//            log.info("Ticket cleanup: no expired tickets found");
//        }
//
//        log.info("Ticket cleanup job finished at {}", LocalDateTime.now());
//    }
}
package ma.bank.ticketmanagementsystembackend.repositories;


import ma.bank.ticketmanagementsystembackend.entities.Client;
import ma.bank.ticketmanagementsystembackend.entities.ClientStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findByStatus(ClientStatus status);
//    List<Client> findByCompany(String company);
//    Optional<Client> findByEmailOrName(String email, String name);
}

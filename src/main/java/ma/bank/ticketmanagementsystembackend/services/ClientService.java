package ma.bank.ticketmanagementsystembackend.services;

import ma.bank.ticketmanagementsystembackend.dtos.dto.ClientDTO;
import ma.bank.ticketmanagementsystembackend.entities.Client;
import ma.bank.ticketmanagementsystembackend.entities.ClientStatus;
import java.util.List;

public interface ClientService  {
    public ClientDTO createClient(Client client);
    public ClientDTO updateClient(Client client);
    public ClientDTO getClientById(Long id);
    public List<ClientDTO> getAllClients();
    public List<ClientDTO> getClientsByStatus(ClientStatus status);
}

package ma.bank.ticketmanagementsystembackend.mappers;

import ma.bank.ticketmanagementsystembackend.dtos.dto.AppUserDTO;
import ma.bank.ticketmanagementsystembackend.dtos.dto.ClientDTO;
import ma.bank.ticketmanagementsystembackend.entities.Client;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ClientMapper {

    private final AppUserMapper appUserMapper;

    public ClientMapper(AppUserMapper appUserMapper) {
        this.appUserMapper = appUserMapper;
    }

    public ClientDTO toDTO(Client client) {
        if (client == null) {
            return null;
        }

        return ClientDTO.builder()
                .clientId(client.getClientId())
                .name(client.getName())
                .email(client.getEmail())
                .phone(client.getPhone())
                .company(client.getCompany())
                .status(client.getStatus())
                .suspensionReason(client.getSuspensionReason())
                .suspendedAt(client.getSuspendedAt())
                .suspendedBy(client.getSuspendedBy())
                .createdAt(client.getCreatedAt())
                .ticketIds(client.getTickets() != null ?
                        client.getTickets().stream()
                                .map(ticket -> ticket.getTicketId())
                                .collect(Collectors.toSet()) : null)
                .employees(client.getEmployees() != null ?
                        client.getEmployees().stream()
                                .map(appUserMapper::toDTO)
                                .collect(Collectors.toSet()) : null)
                .build();
    }

    public Client toEntity(ClientDTO dto) {
        if (dto == null) {
            return null;
        }

        return Client.builder()
                .clientId(dto.getClientId())
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .company(dto.getCompany())
                .status(dto.getStatus())
                .createdAt(dto.getCreatedAt())
                .build();
    }
}
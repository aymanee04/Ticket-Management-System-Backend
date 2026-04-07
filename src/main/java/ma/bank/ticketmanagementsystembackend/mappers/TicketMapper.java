package ma.bank.ticketmanagementsystembackend.mappers;

import ma.bank.ticketmanagementsystembackend.dtos.dto.TicketDTO;
import ma.bank.ticketmanagementsystembackend.entities.Client;
import ma.bank.ticketmanagementsystembackend.entities.Ticket;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class TicketMapper {

    private final AttachmentMapper attachmentMapper;

    public TicketMapper(AttachmentMapper attachmentMapper) {
        this.attachmentMapper = attachmentMapper;
    }

    public TicketDTO toDTO(Ticket ticket) {
        if (ticket == null) {
            return null;
        }

        return TicketDTO.builder()
                .ticketId(ticket.getTicketId())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .status(ticket.getStatus())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .validatedAt(ticket.getValidatedAt())
                .validationComment(ticket.getValidationComment())
                .createdById(ticket.getCreatedBy() != null ? ticket.getCreatedBy().getUserId() : null)
                .createdByName(ticket.getCreatedBy() != null ? ticket.getCreatedBy().getName() : null)
                .assignedToId(ticket.getAssignedTo() != null ? ticket.getAssignedTo().getUserId() : null)
                .assignedToName(ticket.getAssignedTo() != null ? ticket.getAssignedTo().getName() : null)
                .validatedById(ticket.getValidatedBy() != null ? ticket.getValidatedBy().getUserId() : null)
                .attachments(ticket.getAttachments() != null ?
                        ticket.getAttachments().stream()
                                .map(attachmentMapper::toDTO)
                                .collect(Collectors.toList()) : null)
                .clientIds(ticket.getClients() != null ?
                        ticket.getClients().stream()
                                .map(Client::getClientId)
                                .collect(Collectors.toSet()) : null)
                .clientName(ticket.getClients()!=null ? ticket.getClients().stream()
                        .map(Client::getCompany)
                        .collect(Collectors.toSet()) : null)
                .build();
    }

    public Ticket toEntity(TicketDTO dto) {
        if (dto == null) {
            return null;
        }

        return Ticket.builder()
                .ticketId(dto.getTicketId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .status(dto.getStatus())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .validatedAt(dto.getValidatedAt())
                .validationComment(dto.getValidationComment())
                .build();
    }
}
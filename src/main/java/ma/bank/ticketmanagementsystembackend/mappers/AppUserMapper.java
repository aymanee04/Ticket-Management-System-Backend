package ma.bank.ticketmanagementsystembackend.mappers;

import ma.bank.ticketmanagementsystembackend.dtos.dto.AppUserDTO;
import ma.bank.ticketmanagementsystembackend.entities.AppUser;
import org.springframework.stereotype.Component;

@Component
public class AppUserMapper {

    public AppUserDTO toDTO(AppUser user) {
        if (user == null) {
            return null;
        }

        return AppUserDTO.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .jobTitle(user.getJobTitle())
                .email(user.getEmail())
                .roles(user.getRoles())
                .clientId(user.getClient() != null ? user.getClient().getClientId() : null)
                .clientName(user.getClient() != null ? user.getClient().getName() : null)
                .build();
    }

    public AppUser toEntity(AppUserDTO dto) {
        if (dto == null) {
            return null;
        }

        return AppUser.builder()
                .userId(dto.getUserId())
                .name(dto.getName())
                .jobTitle(dto.getJobTitle())
                .email(dto.getEmail())
                .roles(dto.getRoles())
                .build();
    }
}
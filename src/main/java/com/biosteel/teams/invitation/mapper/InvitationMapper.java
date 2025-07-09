package com.biosteel.teams.invitation.mapper;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.biosteel.teams.invitation.dto.InvitationRequestDTO;
import com.biosteel.teams.invitation.dto.InvitationResponseDTO;
import com.biosteel.teams.invitation.model.Invitation;
import com.biosteel.teams.invitation.model.Invitation.InvitationStatus;
import com.biosteel.teams.team.dto.TeamDTO;

@Component
public class InvitationMapper {

    public Invitation toEntity(InvitationRequestDTO dto) {
        return Invitation.builder()
                .invitationId(UUID.randomUUID())
                .teamId(dto.getTeamId())
                .eventId(dto.getEventId())
                .email(dto.getEmail())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .teamRole(dto.getTeamRole())
                .invitationStatus(InvitationStatus.PENDING)
                .createdAt(ZonedDateTime.now())
                .build();
    }

    public InvitationResponseDTO toDto(Invitation entity, TeamDTO team) {
        return InvitationResponseDTO.builder()
                .invitationId(entity.getInvitationId())
                .team(team)
                .eventId(entity.getEventId())
                .userId(entity.getUserId())
                .email(entity.getEmail())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .teamRole(entity.getTeamRole())
                .invitationCode(entity.getInvitationCode())
                .invitationStatus(entity.getInvitationStatus())
                .expiresAt(entity.getExpiresAt())
                .usedAt(entity.getUsedAt())
                .createdAt(entity.getCreatedAt())
                .invitation(entity)
                .build();
    }

    public List<InvitationResponseDTO> toDtoList(List<Invitation> entities, TeamDTO team) {
        return entities.stream()
                .map(entity -> this.toDto(entity, team))
                .collect(Collectors.toList());
    }
}
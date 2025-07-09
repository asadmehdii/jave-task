package com.biosteel.teams.invitation.dto;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.biosteel.teams.invitation.model.Invitation;
import com.biosteel.teams.invitation.model.Invitation.InvitationStatus;
import com.biosteel.teams.team.dto.TeamDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationResponseDTO {
    private UUID invitationId;
    private TeamDTO team;
    private UUID eventId;
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private String teamRole;
    private String invitationCode;
    private InvitationStatus invitationStatus;
    private ZonedDateTime expiresAt;
    private ZonedDateTime usedAt;
    private ZonedDateTime createdAt;

    @JsonIgnore
    private Invitation invitation;
}
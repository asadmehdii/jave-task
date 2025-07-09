package com.biosteel.teams.team.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberMinimalDTO {
    private UUID teamMemberUuid;
    private UUID teamId;
    private UUID userId;
    private UUID playerId;
    private Boolean isPlayer;
    private String email;
    private String firstName;
    private String lastName;
    private UUID logoMediaId;
    private String role;
    private LocalDateTime joinedAt;
}
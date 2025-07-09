package com.biosteel.teams.team.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.biosteel.teams.sport.dto.AttributeValueDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TeamMemberRequestDTO {
    @NotNull(message = "Member type is required")
    private Boolean isPlayer;
    private UUID id;

    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String gender;
    private UUID logoMediaId;

    private List<AttributeValueDTO> playerAttributes;
    private Boolean inviteToJoinApp = false;

    @Email(message = "Valid email is required when inviting to join app")
    private String email;
    private String phone;

    @Valid
    private List<ContactRequestDTO> contacts;

    private String role;
    private UUID userId;
    private UUID playerId;
    private UUID contactId;
}
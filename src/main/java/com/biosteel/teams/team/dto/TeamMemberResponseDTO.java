package com.biosteel.teams.team.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.biosteel.teams.invitation.dto.InvitationResponseDTO;
import com.biosteel.teams.sport.dto.AttributeValueDTO;
import com.biosteel.teams.user.dto.UserDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberResponseDTO {

    // Basic team member info
    private UUID teamMemberUuid;
    private UUID teamId;
    private String role;
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;

    // Member type
    private Boolean isPlayer;
    private Boolean isContact;
    private Boolean isAdmin;

    // Basic person information
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private String gender;
    private UUID logoMediaId;
    private String relationshipType;

    // Player-specific information
    private UUID playerId;
    private String jerseyNumber;
    private String primaryPosition;
    private String secondaryPosition;
    private List<AttributeValueDTO> playerAttributes;

    // User-specific information
    private UUID userId;
    private UserDto parentUser; // For players with parent users

    // Contact-specific information
    private UUID contactId;
    private List<ContactResponseDTO> contacts; // Contacts associated with this member

    // Invitation information
    private UUID invitationId;
    private InvitationResponseDTO invitation;

}
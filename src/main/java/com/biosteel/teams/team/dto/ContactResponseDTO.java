package com.biosteel.teams.team.dto;

import java.util.UUID;

import com.biosteel.teams.invitation.dto.InvitationResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactResponseDTO {
    private UUID playerContactId;
    private UUID contactMemberId;
    private UUID contactId;

    // Contact details
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private UUID logoMediaId;

    // Relationship details
    private String relationshipType;
    private Boolean isPrimaryContact;
    private Boolean canPickup;
    private Boolean isEmergencyContact;
    private Boolean canViewMedicalInfo;
    private Boolean receivesNotifications;
    private String notes;

    // Invitation information
    private UUID invitationId;
    private InvitationResponseDTO invitation;
}

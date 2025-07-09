package com.biosteel.teams.team.dto;

import java.util.UUID;

import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ContactRequestDTO {
    private String firstName;
    private String lastName;
    @Email(message = "Valid email is required for contact")
    private String email;
    private String phone;
    private String relationshipType; // Parent/Guardian, Mother, Father, etc.
    private Boolean isPrimaryContact = false;
    private Boolean canPickup = false;
    private Boolean isEmergencyContact = false;
    private Boolean canViewMedicalInfo = false;
    private Boolean receivesNotifications = true;
    private String notes;
    private UUID contactId;
    private UUID logoMediaId;
}

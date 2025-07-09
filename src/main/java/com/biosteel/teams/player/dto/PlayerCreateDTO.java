package com.biosteel.teams.player.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerCreateDTO {
    private String firstName;
    private String lastName;
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;
    private String gender;
    private String jerseyNumber;
    private Integer heightCm;
    private BigDecimal weightKg;
    private String primaryPosition;
    private String secondaryPosition;
    private String medicalNotes;
    private UUID logoMediaId;
}
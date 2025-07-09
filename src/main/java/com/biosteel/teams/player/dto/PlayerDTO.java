package com.biosteel.teams.player.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerDTO {
    private UUID playerId;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private UUID logoMediaId;
    private String gender;
    private String jerseyNumber;
    private Integer heightCm;
    private BigDecimal weightKg;
    private String primaryPosition;
    private String secondaryPosition;
    private String medicalNotes;
}

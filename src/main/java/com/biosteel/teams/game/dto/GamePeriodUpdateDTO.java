package com.biosteel.teams.game.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GamePeriodUpdateDTO {
    @NotNull(message = "Period definition ID is required")
    private UUID periodDefinitionId;
}
package com.biosteel.teams.game.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateGameStatusRequestDTO {
    @NotNull(message = "Game status is required")
    private String status;
}

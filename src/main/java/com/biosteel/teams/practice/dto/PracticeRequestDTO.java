package com.biosteel.teams.practice.dto;

import java.time.ZonedDateTime;

import com.biosteel.teams.event.dto.LocationDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PracticeRequestDTO {
    @NotBlank
    @Size(max = 255)
    private String title;

    @Size(max = 1000)
    private String description;

    private LocationDTO location;

    @NotNull
    private ZonedDateTime startTime;

    @NotNull
    private ZonedDateTime endTime;
    private ZonedDateTime arrivalTime;

    @Size(max = 2000)
    private String instructions;
}

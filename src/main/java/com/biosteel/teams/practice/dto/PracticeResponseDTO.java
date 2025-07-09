package com.biosteel.teams.practice.dto;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.biosteel.teams.event.dto.LocationDTO;

import lombok.Data;

@Data
public class PracticeResponseDTO {
    private UUID practiceId;
    private String title;
    private String description;
    private LocationDTO location;
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
    private ZonedDateTime arrivalTime;
    private String instructions;
    private String eventStatus;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}

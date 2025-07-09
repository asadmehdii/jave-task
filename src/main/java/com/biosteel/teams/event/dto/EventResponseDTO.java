package com.biosteel.teams.event.dto;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.biosteel.teams.game.dto.GameResponseDTO;
import com.biosteel.teams.practice.dto.PracticeResponseDTO;

import lombok.Data;

@Data
public class EventResponseDTO {
    private UUID eventId;
    private UUID teamId;
    private UUID userId;
    private String title;
    private String description;
    private LocationDTO location;
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
    private ZonedDateTime arrivalTime;
    private String eventType;
    private String eventStatus;
    private Boolean isRecurring;
    private String recurrenceRule;
    private UUID bannerMediaId;
    private UUID logoMediaId;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private GameResponseDTO game;
    private PracticeResponseDTO practice;
    private String streamId;

}
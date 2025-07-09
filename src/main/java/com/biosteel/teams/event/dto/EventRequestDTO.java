package com.biosteel.teams.event.dto;

import java.time.ZonedDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EventRequestDTO {
    @NotNull
    private String title;
    private String description;
    private LocationDTO location;
    @NotNull
    private ZonedDateTime startTime;
    @NotNull
    private ZonedDateTime endTime;
    private ZonedDateTime arrivalTime;
    private String eventType;
    private String eventStatus;
    private Boolean isRecurring;
    private String recurrenceRule;
    private UUID bannerMediaId;
    private UUID logoMediaId;
    private String streamId;
}
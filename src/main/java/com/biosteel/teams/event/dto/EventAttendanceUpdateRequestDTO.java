package com.biosteel.teams.event.dto;

import java.time.ZonedDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EventAttendanceUpdateRequestDTO {
    @NotNull
    private String eventAttendanceStatus;
    private String notes;
    private ZonedDateTime registeredAt;
    private ZonedDateTime checkedInAt;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
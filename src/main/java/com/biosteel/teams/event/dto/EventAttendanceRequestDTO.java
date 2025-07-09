package com.biosteel.teams.event.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EventAttendanceRequestDTO {
    @NotNull
    private UUID teamMemberId;
    @NotNull
    private String eventAttendanceStatus;
    private String notes;
}
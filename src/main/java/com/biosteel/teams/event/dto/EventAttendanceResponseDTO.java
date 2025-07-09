package com.biosteel.teams.event.dto;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.biosteel.teams.team.dto.TeamMemberResponseDTO;

import lombok.Data;

@Data
public class EventAttendanceResponseDTO {
    private UUID eventAttendanceId;
    private UUID eventId;
    private UUID teamMemberId;
    private String eventAttendanceStatus;
    private String notes;
    private ZonedDateTime registeredAt;
    private ZonedDateTime checkedInAt;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private TeamMemberResponseDTO member;
}

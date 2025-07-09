package com.biosteel.teams.event.model;

import java.time.ZonedDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "event_attendance", schema = "biosteel")
@Data
public class EventAttendance {
    @Id
    @Column(name = "event_attendance_id")
    private UUID eventAttendanceId;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "team_member_id")
    private UUID teamMemberId;

    @Column(name = "event_attendance_status")
    private String eventAttendanceStatus;

    @Column(name = "notes")
    private String notes;

    @Column(name = "registered_at")
    private ZonedDateTime registeredAt;

    @Column(name = "checked_in_at")
    private ZonedDateTime checkedInAt;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Column(name = "deleted_at")
    private ZonedDateTime deletedAt;
}
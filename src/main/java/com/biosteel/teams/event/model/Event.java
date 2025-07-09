package com.biosteel.teams.event.model;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.biosteel.teams.game.model.Game;
import com.biosteel.teams.practice.model.Practice;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "event", schema = "biosteel")
@Data
public class Event {
    @Id
    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "team_id", nullable = false)
    private UUID teamId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "event_type")
    private String eventType;

    @Column(name = "event_status")
    private String eventStatus;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "stream_id")
    private String streamId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(name = "location_id", insertable = false, updatable = false)
    private UUID locationId;

    @Column(name = "start_time", nullable = false)
    private ZonedDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private ZonedDateTime endTime;

    @Column(name = "is_recurring")
    private Boolean isRecurring;

    @Column(name = "recurrence_rule")
    private String recurrenceRule;

    @Column(name = "banner_media_id")
    private UUID bannerMediaId;

    @Column(name = "logo_media_id")
    private UUID logoMediaId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private Game game;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "practice_id")
    private Practice practice;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Column(name = "deleted_at")
    private ZonedDateTime deletedAt;

    @Column(name = "arrival_time")
    private ZonedDateTime arrivalTime;

    public UUID getPracticeId() {
        return practice != null ? practice.getPracticeId() : null;
    }

    public UUID getGameId() {
        return game != null ? game.getGameId() : null;
    }
}
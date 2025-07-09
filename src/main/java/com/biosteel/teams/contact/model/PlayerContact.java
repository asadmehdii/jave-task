package com.biosteel.teams.contact.model;

import java.time.ZonedDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "player_contact", schema = "biosteel")
@Builder(toBuilder = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerContact {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "player_contact_id", nullable = false)
    private UUID playerContactId;

    @Column(name = "player_member_id")
    private UUID playerMemberId;

    @Column(name = "contact_member_id")
    private UUID contactMemberId;

    @Column(name = "relationship_type", length = 50)
    private String relationshipType;

    @Column(name = "is_primary_contact")
    private Boolean isPrimaryContact;

    @Column(name = "can_pickup")
    private Boolean canPickup;

    @Column(name = "is_emergency_contact")
    private Boolean isEmergencyContact;

    @Column(name = "can_view_medical_info")
    private Boolean canViewMedicalInfo;

    @Column(name = "receives_notifications")
    private Boolean receivesNotifications;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", columnDefinition = "timestamp with time zone default now()")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Column(name = "deleted_at")
    private ZonedDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = ZonedDateTime.now();
    }
}
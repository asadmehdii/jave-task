package com.biosteel.teams.invitation.model;

import java.time.ZonedDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "invitation", schema = "biosteel")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Invitation {

    public enum InvitationStatus {
        PENDING, ACCEPTED, EXPIRED, CANCELLED
    }

    @Id
    @Column(name = "invitation_id")
    private UUID invitationId;

    @Column(name = "team_id")
    private UUID teamId;

    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "email")
    private String email;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "team_role")
    private String teamRole;

    @Column(name = "invitation_code", unique = true, length = 6)
    private String invitationCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "invitation_status")
    private InvitationStatus invitationStatus;

    @Column(name = "expires_at")
    private ZonedDateTime expiresAt;

    @Column(name = "used_at")
    private ZonedDateTime usedAt;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Column(name = "deleted_at")
    private ZonedDateTime deletedAt;

    public boolean isExpired() {
        return ZonedDateTime.now().isAfter(expiresAt) ||
                InvitationStatus.EXPIRED.equals(invitationStatus);
    }

    public boolean isUsed() {
        return usedAt != null ||
                InvitationStatus.ACCEPTED.equals(invitationStatus);
    }

    public boolean isValid() {
        return !isExpired() && !isUsed() &&
                InvitationStatus.PENDING.equals(invitationStatus);
    }
}
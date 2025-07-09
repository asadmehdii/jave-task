package com.biosteel.teams.team.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "team_member", schema = "biosteel")
@Builder(toBuilder = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeamMember {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "team_member_id")
    private UUID id;

    @Column(name = "team_id")
    private UUID teamId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "player_id")
    private UUID playerId;

    @Column(name = "contact_id")
    private UUID contactId;

    @Column(name = "invitation_id")
    private UUID invitationId;

    @Column(name = "role")
    private String role;

    @Column(name = "is_player")
    private Boolean isPlayer;

    @Column(name = "is_contact")
    private Boolean isContact;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;
}
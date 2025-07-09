package com.biosteel.teams.invitation.repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.biosteel.teams.invitation.model.Invitation;
import com.biosteel.teams.invitation.model.Invitation.InvitationStatus;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, UUID> {

    Optional<Invitation> findByInvitationCode(String invitationCode);

    List<Invitation> findByTeamId(UUID teamId);

    List<Invitation> findByEventId(UUID eventId);

    List<Invitation> findByEmailAndTeamIdAndInvitationStatus(String email, UUID teamId, InvitationStatus status);

    List<Invitation> findByEmailAndInvitationStatus(String email, InvitationStatus status);

    @Query(value = "SELECT * FROM biosteel.invitation i WHERE i.expires_at < ?1 AND i.invitation_status = ?2", nativeQuery = true)
    List<Invitation> findExpiredInvitations(ZonedDateTime now, String status);

    @Query(value = "SELECT * FROM biosteel.invitation i WHERE i.team_id = ?1 AND i.email = ?2 AND i.invitation_status = ?3", nativeQuery = true)
    Optional<Invitation> findActiveInvitationForEmailAndTeam(UUID teamId, String email, String status);
}
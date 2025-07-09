package com.biosteel.teams.team.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.biosteel.teams.team.model.TeamMember;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, UUID> {
    List<TeamMember> findByTeamIdAndLeftAtIsNull(UUID teamId);

    Optional<TeamMember> findByIdAndTeamId(UUID id, UUID teamId);

    boolean existsByTeamIdAndUserIdAndLeftAtIsNull(UUID teamId, UUID userId);

    List<TeamMember> findByUserIdOrPlayerIdInAndLeftAtIsNull(UUID userId, List<UUID> playerIds);

    List<TeamMember> findAllByTeamIdAndUserIdAndLeftAtIsNull(UUID teamId, UUID userId);

    List<TeamMember> findByInvitationIdAndLeftAtIsNull(UUID invitationId);

    List<TeamMember> findByContactIdAndLeftAtIsNull(UUID contactId);

    List<TeamMember> findByTeamIdAndRoleAndLeftAtIsNull(UUID teamId, String role);

    default Optional<TeamMember> findByTeamIdAndUserIdAndLeftAtIsNull(UUID teamId, UUID userId) {
        List<TeamMember> members = findAllByTeamIdAndUserIdAndLeftAtIsNull(teamId, userId);
        return members.isEmpty() ? Optional.empty() : Optional.of(members.get(0));
    }
}
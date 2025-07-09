package com.biosteel.teams.sport.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.biosteel.teams.team.model.TeamMemberAttributeValue;
import com.biosteel.teams.team.model.TeamMemberAttributeValueId;

@Repository
public interface TeamMemberAttributeValueRepository
        extends JpaRepository<TeamMemberAttributeValue, TeamMemberAttributeValueId> {
    List<TeamMemberAttributeValue> findAllByIdTeamMemberId(UUID teamMemberId);
}
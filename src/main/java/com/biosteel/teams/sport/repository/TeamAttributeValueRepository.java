
package com.biosteel.teams.sport.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.biosteel.teams.team.model.TeamAttributeValue;
import com.biosteel.teams.team.model.TeamAttributeValueId;

@Repository
public interface TeamAttributeValueRepository extends JpaRepository<TeamAttributeValue, TeamAttributeValueId> {
    List<TeamAttributeValue> findAllByIdTeamId(UUID teamId);
}
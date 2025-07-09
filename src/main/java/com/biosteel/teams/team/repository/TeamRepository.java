package com.biosteel.teams.team.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.biosteel.teams.team.model.Team;

@Repository
public interface TeamRepository extends JpaRepository<Team, UUID> {
    List<Team> findByUserIdAndDeletedAtIsNull(UUID userId);

    Optional<Team> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    Optional<Team> findByIdAndDeletedAtIsNull(UUID id);

    List<Team> findByIdInAndDeletedAtIsNull(List<UUID> teamIds);

    Page<Team> findAllBySportTypeCodeAndDeletedAtIsNull(
            String sportTypeCode,
            Pageable pageable);
}
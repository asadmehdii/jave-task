package com.biosteel.teams.media.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.biosteel.teams.media.model.Media;

@Repository
public interface MediaRepository extends JpaRepository<Media, UUID> {
    List<Media> findByTeamId(UUID teamId);
}
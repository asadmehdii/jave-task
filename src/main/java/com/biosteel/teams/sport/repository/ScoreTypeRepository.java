package com.biosteel.teams.sport.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.biosteel.teams.sport.model.ScoreType;

public interface ScoreTypeRepository extends JpaRepository<ScoreType, UUID> {
    List<ScoreType> findBySportType(String sportType);
}
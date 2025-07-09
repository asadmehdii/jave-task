package com.biosteel.teams.game.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.biosteel.teams.game.model.PeriodType;

@Repository
public interface PeriodTypeRepository extends JpaRepository<PeriodType, String> {

    List<PeriodType> findAllByDeletedAtIsNull();
}
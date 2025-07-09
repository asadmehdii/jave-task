package com.biosteel.teams.game.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.biosteel.teams.game.model.SportPeriodDefinition;

@Repository
public interface SportPeriodDefinitionRepository extends JpaRepository<SportPeriodDefinition, UUID> {

    List<SportPeriodDefinition> findBySportTypeCodeAndDeletedAtIsNullOrderByDisplayOrder(String sportTypeCode);

    Optional<SportPeriodDefinition> findBySportTypeCodeAndPeriodNumberAndDeletedAtIsNull(String sportTypeCode,
            Integer periodNumber);

    Optional<SportPeriodDefinition> findBySportPeriodDefinitionIdAndDeletedAtIsNull(UUID sportPeriodDefinitionId);

    Optional<SportPeriodDefinition> findFirstBySportTypeCodeAndPeriodNumberGreaterThanAndDeletedAtIsNullOrderByPeriodNumber(
            String sportTypeCode, Integer periodNumber);

    Optional<SportPeriodDefinition> findFirstBySportTypeCodeAndDeletedAtIsNullOrderByPeriodNumber(String sportTypeCode);
}
package com.biosteel.teams.sport.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.biosteel.teams.sport.model.SportAttributeMapping;
import com.biosteel.teams.sport.model.SportAttributeMappingId;

@Repository
public interface SportAttributeMappingRepository extends JpaRepository<SportAttributeMapping, SportAttributeMappingId> {
    List<SportAttributeMapping> findBySportTypeCode(String sportTypeCode);

    List<SportAttributeMapping> findBySportTypeCodeAndAttributeDefinitionEntityType(String sportTypeCode,
            String entityType);
}
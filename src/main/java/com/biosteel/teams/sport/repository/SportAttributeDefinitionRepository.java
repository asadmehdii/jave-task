package com.biosteel.teams.sport.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.biosteel.teams.sport.model.SportAttributeDefinition;

public interface SportAttributeDefinitionRepository extends JpaRepository<SportAttributeDefinition, String> {

    Optional<SportAttributeDefinition> findByCode(String code);

    List<SportAttributeDefinition> findByEntityType(String entityType);

    @Query("SELECT sad FROM SportAttributeDefinition sad " +
            "JOIN SportAttributeMapping sam ON sad.code = sam.id.attributeCode " +
            "WHERE sam.id.sportTypeCode = :sportTypeCode " +
            "AND sad.entityType = :entityType " +
            "AND sad.deletedAt IS NULL " +
            "ORDER BY sam.displayOrder")
    List<SportAttributeDefinition> findBySportTypeAndEntityType(
            @Param("sportTypeCode") String sportTypeCode,
            @Param("entityType") String entityType);

    @Query("SELECT sad FROM SportAttributeDefinition sad " +
            "WHERE sad.code IN :codes " +
            "AND sad.deletedAt IS NULL")
    List<SportAttributeDefinition> findByCodesAndNotDeleted(@Param("codes") List<String> codes);
}
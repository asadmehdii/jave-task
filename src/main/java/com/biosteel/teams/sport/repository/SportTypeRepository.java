package com.biosteel.teams.sport.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.biosteel.teams.sport.model.SportType;

public interface SportTypeRepository extends JpaRepository<SportType, String> {
    Optional<SportType> findByCode(String code);

    boolean existsByCode(String sportTypeCode);
}
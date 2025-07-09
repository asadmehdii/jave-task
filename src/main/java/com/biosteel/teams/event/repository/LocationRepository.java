package com.biosteel.teams.event.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.biosteel.teams.event.model.Location;

public interface LocationRepository extends JpaRepository<Location, UUID> {
}
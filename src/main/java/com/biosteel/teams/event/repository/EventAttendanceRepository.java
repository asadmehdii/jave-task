package com.biosteel.teams.event.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.biosteel.teams.event.model.EventAttendance;

@Repository
public interface EventAttendanceRepository extends JpaRepository<EventAttendance, UUID> {
    List<EventAttendance> findByEventId(UUID eventId);
}
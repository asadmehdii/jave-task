package com.biosteel.teams.notification.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.biosteel.teams.notification.model.NotificationLog;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {

    Page<NotificationLog> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<NotificationLog> findByTeamIdOrderByCreatedAtDesc(UUID teamId, Pageable pageable);
}
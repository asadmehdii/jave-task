package com.biosteel.teams.audit.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(schema = "biosteel", name = "audit_log")
@Data
public class AuditLog {

    @Id
    @Column(name = "audit_log_id")
    private UUID auditLogId;

    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "action")
    private String action;

    @Column(name = "action_status")
    private String actionStatus;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "email")
    private String email;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "message")
    private String message;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "extra_data")
    private String extraData;
}
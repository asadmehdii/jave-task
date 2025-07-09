package com.biosteel.teams.audit.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.biosteel.teams.audit.model.AuditLog;
import com.biosteel.teams.audit.repository.AuditLogRepository;
import com.biosteel.teams.user.model.User;
import com.biosteel.teams.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExceptionAuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserService userService;

    private final ObjectMapper objectMapper = createAuditObjectMapper();

    private static ObjectMapper createAuditObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    /**
     * Log exception to audit table
     * Uses REQUIRES_NEW propagation to ensure audit logging happens even if main
     * transaction fails
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logException(Exception exception, HttpServletRequest request, String action) {
        try {
            AuditLog auditLog = createAuditLogEntry(exception, request, action);
            auditLogRepository.save(auditLog);
            log.debug("Exception audit logged successfully for action: {}", action);
        } catch (Exception e) {
            log.error("Failed to save exception audit log for action {}: {}", action, e.getMessage());
        }
    }

    /**
     * Log exception with additional context data
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logExceptionWithContext(Exception exception, HttpServletRequest request,
            String action, Object contextData) {
        try {
            AuditLog auditLog = createAuditLogEntry(exception, request, action);

            if (contextData != null) {
                Map<String, Object> auditContext = new HashMap<>();

                auditContext.put("exception", new ExceptionDetails(exception));

                if (contextData instanceof Map) {
                    auditContext.putAll((Map<String, Object>) contextData);
                } else {
                    auditContext.put("contextData", contextData);
                }

                auditContext.put("stackTrace", getStackTraceAsString(exception));

                auditLog.setExtraData(objectMapper.writeValueAsString(auditContext));
            }

            auditLogRepository.save(auditLog);
            log.debug("Exception audit with context logged successfully for action: {}", action);
        } catch (Exception e) {
            log.error("Failed to save exception audit log with context for action {}: {}", action, e.getMessage());
        }
    }

    private AuditLog createAuditLogEntry(Exception exception, HttpServletRequest request, String action) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAuditLogId(UUID.randomUUID());
        auditLog.setDate(LocalDateTime.now());
        auditLog.setAction(action);
        auditLog.setActionStatus("FAILED");

        try {
            User currentUser = userService.getLoggedInUser();
            if (currentUser != null) {
                auditLog.setUserId(currentUser.getUserId());
                auditLog.setEmail(currentUser.getEmail());
            }
        } catch (Exception e) {
            log.debug("Could not get current user for audit log: {}", e.getMessage());
        }

        if (request != null) {
            auditLog.setIpAddress(getClientIpAddress(request));
            auditLog.setSessionId(extractSessionId(request));
            auditLog.setUserAgent(request.getHeader("User-Agent"));
        }

        String message = String.format("Exception: %s - %s",
                exception.getClass().getSimpleName(),
                exception.getMessage());

        if (message.length() > 1000) {
            message = message.substring(0, 997) + "...";
        }
        auditLog.setMessage(message);

        return auditLog;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        try {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
                return xForwardedFor.split(",")[0].trim();
            }

            String xRealIp = request.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
                return xRealIp;
            }

            String xOriginalForwardedFor = request.getHeader("X-Original-Forwarded-For");
            if (xOriginalForwardedFor != null && !xOriginalForwardedFor.isEmpty()
                    && !"unknown".equalsIgnoreCase(xOriginalForwardedFor)) {
                return xOriginalForwardedFor.split(",")[0].trim();
            }

            return request.getRemoteAddr();
        } catch (Exception e) {
            log.debug("Error extracting IP address: {}", e.getMessage());
            return "unknown";
        }
    }

    private String extractSessionId(HttpServletRequest request) {
        try {
            return request.getSession(false) != null ? request.getSession().getId() : null;
        } catch (Exception e) {
            log.debug("Error extracting session ID: {}", e.getMessage());
            return null;
        }
    }

    private String getStackTraceAsString(Exception exception) {
        try {
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            exception.printStackTrace(pw);

            String stackTrace = sw.toString();
            return stackTrace.length() > 5000 ? stackTrace.substring(0, 4997) + "..." : stackTrace;
        } catch (Exception e) {
            log.debug("Error extracting stack trace: {}", e.getMessage());
            return "Stack trace extraction failed";
        }
    }

    public static class ExceptionDetails {
        private String type;
        private String message;
        private String cause;

        public ExceptionDetails(Exception exception) {
            this.type = exception.getClass().getName();
            this.message = exception.getMessage();
            this.cause = exception.getCause() != null ? exception.getCause().toString() : null;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getCause() {
            return cause;
        }

        public void setCause(String cause) {
            this.cause = cause;
        }
    }
}
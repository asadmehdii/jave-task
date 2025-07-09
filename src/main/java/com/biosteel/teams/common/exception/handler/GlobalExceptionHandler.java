package com.biosteel.teams.common.exception.handler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.biosteel.teams.audit.service.ExceptionAuditService;
import com.biosteel.teams.common.exception.ForbiddenException;
import com.biosteel.teams.common.exception.InvalidCredentialsException;
import com.biosteel.teams.common.exception.InvalidTokenException;
import com.biosteel.teams.common.exception.ResourceNotFoundException;
import com.biosteel.teams.common.model.response.ErrorResponse;
import com.biosteel.teams.common.model.response.ValidationErrorResponse;
import com.biosteel.teams.user.exception.UserAlreadyExistsException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

        private final ExceptionAuditService exceptionAuditService;

        @ExceptionHandler(ResourceNotFoundException.class)
        @ResponseStatus(HttpStatus.NOT_FOUND)
        public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
                        ResourceNotFoundException ex,
                        HttpServletRequest request) {

                String action = getActionFromRequest(request);
                exceptionAuditService.logException(ex, request, action + "_RESOURCE_NOT_FOUND");

                ErrorResponse response = ErrorResponse.builder()
                                .status(HttpStatus.NOT_FOUND.value())
                                .message(ex.getMessage())
                                .timestamp(LocalDateTime.now())
                                .path(request.getRequestURI())
                                .build();
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(UserAlreadyExistsException.class)
        @ResponseStatus(HttpStatus.CONFLICT)
        public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(
                        UserAlreadyExistsException ex,
                        HttpServletRequest request) {

                exceptionAuditService.logException(ex, request, "USER_REGISTRATION_CONFLICT");

                ErrorResponse response = ErrorResponse.builder()
                                .status(HttpStatus.CONFLICT.value())
                                .message(ex.getMessage())
                                .timestamp(LocalDateTime.now())
                                .path(request.getRequestURI())
                                .build();
                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }

        @ExceptionHandler(InvalidTokenException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public ResponseEntity<ErrorResponse> handleInvalidTokenException(
                        InvalidTokenException ex,
                        HttpServletRequest request) {

                Map<String, Object> context = new HashMap<>();
                context.put("tokenType", "authentication");
                context.put("securityIncident", true);
                exceptionAuditService.logExceptionWithContext(ex, request, "INVALID_TOKEN_ERROR", context);

                ErrorResponse response = ErrorResponse.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message(ex.getMessage())
                                .timestamp(LocalDateTime.now())
                                .path(request.getRequestURI())
                                .build();
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
        @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
        public ResponseEntity<ErrorResponse> handleMethodNotSupportedException(
                        org.springframework.web.HttpRequestMethodNotSupportedException ex,
                        HttpServletRequest request) {

                String action = getActionFromRequest(request);
                Map<String, Object> context = new HashMap<>();
                context.put("unsupportedMethod", request.getMethod());
                context.put("supportedMethods", ex.getSupportedMethods());
                exceptionAuditService.logExceptionWithContext(ex, request, action + "_METHOD_NOT_ALLOWED", context);

                ErrorResponse response = ErrorResponse.builder()
                                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                                .message("HTTP method '" + request.getMethod() + "' not supported for this endpoint")
                                .timestamp(LocalDateTime.now())
                                .path(request.getRequestURI())
                                .build();
                return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
        }

        @ExceptionHandler(org.springframework.web.servlet.NoHandlerFoundException.class)
        @ResponseStatus(HttpStatus.NOT_FOUND)
        public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(
                        org.springframework.web.servlet.NoHandlerFoundException ex,
                        HttpServletRequest request) {

                exceptionAuditService.logException(ex, request, "ENDPOINT_NOT_FOUND");

                ErrorResponse response = ErrorResponse.builder()
                                .status(HttpStatus.NOT_FOUND.value())
                                .message("Endpoint not found")
                                .timestamp(LocalDateTime.now())
                                .path(request.getRequestURI())
                                .build();
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
                        org.springframework.web.bind.MissingServletRequestParameterException ex,
                        HttpServletRequest request) {

                String action = getActionFromRequest(request);
                Map<String, Object> context = new HashMap<>();
                context.put("missingParameter", ex.getParameterName());
                context.put("parameterType", ex.getParameterType());
                exceptionAuditService.logExceptionWithContext(ex, request, action + "_MISSING_PARAMETER", context);

                ErrorResponse response = ErrorResponse.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message("Required parameter '" + ex.getParameterName() + "' is missing")
                                .timestamp(LocalDateTime.now())
                                .path(request.getRequestURI())
                                .build();
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(org.springframework.dao.DataAccessException.class)
        @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        public ResponseEntity<ErrorResponse> handleDataAccessException(
                        org.springframework.dao.DataAccessException ex,
                        HttpServletRequest request) {

                String action = getActionFromRequest(request);
                Map<String, Object> context = new HashMap<>();
                context.put("databaseError", true);

                if (ex.getCause() instanceof java.sql.SQLException) {
                        java.sql.SQLException sqlEx = (java.sql.SQLException) ex.getCause();
                        context.put("sqlState", sqlEx.getSQLState());
                        context.put("errorCode", sqlEx.getErrorCode());
                }

                exceptionAuditService.logExceptionWithContext(ex, request, action + "_DATABASE_ERROR", context);

                log.error("Database error for action: {}: {}", action, ex.getMessage(), ex);

                ErrorResponse response = ErrorResponse.builder()
                                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .message("Database operation failed")
                                .timestamp(LocalDateTime.now())
                                .path(request.getRequestURI())
                                .build();
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(
                        MethodArgumentNotValidException ex,
                        HttpServletRequest request) {

                Map<String, String> errors = new HashMap<>();
                ex.getBindingResult().getAllErrors().forEach(error -> {
                        String fieldName = ((FieldError) error).getField();
                        String errorMessage = error.getDefaultMessage();
                        errors.put(fieldName, errorMessage);
                });

                String action = getActionFromRequest(request);
                Map<String, Object> context = new HashMap<>();
                context.put("validationErrors", errors);
                context.put("fieldCount", errors.size());
                exceptionAuditService.logExceptionWithContext(ex, request, action + "_VALIDATION_ERROR", context);

                ValidationErrorResponse response = new ValidationErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "Validation failed",
                                LocalDateTime.now(),
                                request.getRequestURI(),
                                errors);
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
                        org.springframework.http.converter.HttpMessageNotReadableException ex,
                        HttpServletRequest request) {

                String action = getActionFromRequest(request);
                Map<String, Object> context = new HashMap<>();
                context.put("jsonParseError", true);
                context.put("contentType", request.getContentType());
                context.put("contentLength", request.getContentLength());

                String errorDetail = "Invalid JSON format";
                if (ex.getCause() instanceof com.fasterxml.jackson.core.JsonParseException) {
                        com.fasterxml.jackson.core.JsonParseException jsonEx = (com.fasterxml.jackson.core.JsonParseException) ex
                                        .getCause();
                        errorDetail = "JSON parse error at line " + jsonEx.getLocation().getLineNr() +
                                        ", column " + jsonEx.getLocation().getColumnNr();
                        context.put("jsonError", Map.of(
                                        "line", jsonEx.getLocation().getLineNr(),
                                        "column", jsonEx.getLocation().getColumnNr(),
                                        "originalMessage", jsonEx.getOriginalMessage()));
                }

                exceptionAuditService.logExceptionWithContext(ex, request, action + "_JSON_PARSE_ERROR", context);

                log.warn("JSON parse error for action: {} - {}", action, ex.getMessage());

                ErrorResponse response = ErrorResponse.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message("Invalid JSON format in request body")
                                .timestamp(LocalDateTime.now())
                                .path(request.getRequestURI())
                                .build();
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(AuthenticationException.class)
        @ResponseStatus(HttpStatus.UNAUTHORIZED)
        public ResponseEntity<ErrorResponse> handleAuthenticationException(
                        AuthenticationException ex,
                        HttpServletRequest request) {

                Map<String, Object> context = new HashMap<>();
                context.put("authenticationFailure", true);
                context.put("securityIncident", true);
                context.put("userAgent", request.getHeader("User-Agent"));
                exceptionAuditService.logExceptionWithContext(ex, request, "AUTHENTICATION_FAILED", context);

                ErrorResponse response = ErrorResponse.builder()
                                .status(HttpStatus.UNAUTHORIZED.value())
                                .message("Authentication failed")
                                .timestamp(LocalDateTime.now())
                                .path(request.getRequestURI())
                                .build();
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        @ExceptionHandler(AccessDeniedException.class)
        @ResponseStatus(HttpStatus.FORBIDDEN)
        public ResponseEntity<ErrorResponse> handleAccessDeniedException(
                        AccessDeniedException ex,
                        HttpServletRequest request) {

                String action = getActionFromRequest(request);
                Map<String, Object> context = new HashMap<>();
                context.put("accessDenied", true);
                context.put("securityIncident", true);
                context.put("attemptedAction", action);
                exceptionAuditService.logExceptionWithContext(ex, request, action + "_ACCESS_DENIED", context);

                ErrorResponse response = ErrorResponse.builder()
                                .status(HttpStatus.FORBIDDEN.value())
                                .message("Access denied")
                                .timestamp(LocalDateTime.now())
                                .path(request.getRequestURI())
                                .build();
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }

        @ExceptionHandler(InvalidCredentialsException.class)
        public ResponseEntity<ErrorResponse> handleInvalidCredentials(
                        InvalidCredentialsException ex,
                        HttpServletRequest request) {

                Map<String, Object> context = new HashMap<>();
                context.put("invalidCredentials", true);
                context.put("securityIncident", true);
                context.put("loginAttempt", true);
                exceptionAuditService.logExceptionWithContext(ex, request, "INVALID_CREDENTIALS_ERROR", context);

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message(ex.getMessage())
                                .timestamp(LocalDateTime.now())
                                .path(request != null ? request.getRequestURI() : null)
                                .build();
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(ForbiddenException.class)
        @ResponseStatus(HttpStatus.FORBIDDEN)
        public ErrorResponse handleForbidden(ForbiddenException ex, HttpServletRequest request) {

                String action = getActionFromRequest(request);
                exceptionAuditService.logException(ex, request, action + "_FORBIDDEN_ERROR");

                return ErrorResponse.builder()
                                .status(HttpStatus.FORBIDDEN.value())
                                .message(ex.getMessage())
                                .timestamp(LocalDateTime.now())
                                .path(request != null ? request.getRequestURI() : null)
                                .build();
        }

        @ExceptionHandler(Exception.class)
        @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        public ResponseEntity<ErrorResponse> handleAllUncaughtException(
                        Exception ex,
                        HttpServletRequest request) {

                log.error("Unexpected error occurred", ex);

                String action = getActionFromRequest(request);
                Map<String, Object> context = new HashMap<>();
                context.put("unexpectedError", true);
                context.put("exceptionType", ex.getClass().getSimpleName());
                context.put("requestMethod", request.getMethod());
                context.put("requestUri", request.getRequestURI());
                context.put("queryString", request.getQueryString());
                context.put("contentType", request.getContentType());

                exceptionAuditService.logExceptionWithContext(ex, request, action + "_UNEXPECTED_ERROR", context);

                ErrorResponse response = ErrorResponse.builder()
                                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .message("An unexpected error occurred")
                                .timestamp(LocalDateTime.now())
                                .path(request.getRequestURI())
                                .build();
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        /**
         * Extract meaningful action name from HTTP request
         */
        private String getActionFromRequest(HttpServletRequest request) {
                if (request == null) {
                        return "UNKNOWN_ACTION";
                }

                String method = request.getMethod();
                String uri = request.getRequestURI();

                if (uri.contains("/teams")) {
                        if (uri.contains("/events")) {
                                if (uri.contains("/attendance")) {
                                        if (method.equals("POST"))
                                                return "CREATE_EVENT_ATTENDANCE";
                                        if (method.equals("PUT"))
                                                return "UPDATE_EVENT_ATTENDANCE";
                                        if (method.equals("DELETE"))
                                                return "DELETE_EVENT_ATTENDANCE";
                                        if (method.equals("GET"))
                                                return "GET_EVENT_ATTENDANCE";
                                }
                                if (method.equals("POST"))
                                        return "CREATE_EVENT";
                                if (method.equals("PUT"))
                                        return "UPDATE_EVENT";
                                if (method.equals("DELETE"))
                                        return "DELETE_EVENT";
                                if (method.equals("GET"))
                                        return "GET_EVENT";
                        }
                        if (uri.contains("/members")) {
                                if (uri.contains("/attributes")) {
                                        if (method.equals("POST"))
                                                return "UPDATE_TEAM_MEMBER_ATTRIBUTES";
                                        if (method.equals("GET"))
                                                return "GET_TEAM_MEMBER_ATTRIBUTES";
                                }
                                if (method.equals("POST"))
                                        return "ADD_TEAM_MEMBER";
                                if (method.equals("PUT"))
                                        return "UPDATE_TEAM_MEMBER";
                                if (method.equals("DELETE"))
                                        return "REMOVE_TEAM_MEMBER";
                                if (method.equals("GET"))
                                        return "GET_TEAM_MEMBERS";
                        }
                        if (uri.contains("/attributes")) {
                                if (method.equals("POST"))
                                        return "UPDATE_TEAM_ATTRIBUTES";
                                if (method.equals("GET"))
                                        return "GET_TEAM_ATTRIBUTES";
                        }
                        if (uri.contains("/live"))
                                return "GET_TEAM_LIVE";
                        if (uri.contains("/feed"))
                                return "GET_TEAM_FEED";
                        if (method.equals("POST"))
                                return "CREATE_TEAM";
                        if (method.equals("PUT"))
                                return "UPDATE_TEAM";
                        if (method.equals("DELETE"))
                                return "DELETE_TEAM";
                        if (method.equals("GET"))
                                return "GET_TEAM";
                }

                if (uri.contains("/users")) {
                        if (uri.contains("/register")) {
                                if (uri.contains("/verify")) {
                                        if (uri.contains("/resend"))
                                                return "RESEND_VERIFICATION";
                                        return "VERIFY_REGISTRATION";
                                }
                                return "USER_REGISTRATION";
                        }
                        if (uri.contains("/avatar"))
                                return "USER_AVATAR";
                        if (uri.contains("/changePassword"))
                                return "CHANGE_PASSWORD";
                        if (uri.contains("/resetPassword")) {
                                if (uri.contains("/request"))
                                        return "REQUEST_PASSWORD_RESET";
                                if (uri.contains("/confirm"))
                                        return "RESET_PASSWORD";
                        }
                        if (uri.contains("/me"))
                                return "GET_CURRENT_USER";
                        if (method.equals("POST"))
                                return "CREATE_USER";
                        if (method.equals("PUT"))
                                return "UPDATE_USER";
                        if (method.equals("DELETE"))
                                return "DELETE_USER";
                        if (method.equals("GET"))
                                return "GET_USER";
                }

                if (uri.contains("/players")) {
                        if (method.equals("POST"))
                                return "CREATE_PLAYER";
                        if (method.equals("PUT"))
                                return "UPDATE_PLAYER";
                        if (method.equals("DELETE"))
                                return "DELETE_PLAYER";
                        if (method.equals("GET"))
                                return "GET_PLAYER";
                }

                if (uri.contains("/login"))
                        return "LOGIN";
                if (uri.contains("/logout"))
                        return "LOGOUT";
                if (uri.contains("/auth"))
                        return "AUTHENTICATION";
                if (uri.contains("/admin"))
                        return "ADMIN_OPERATION";

                String cleanUri = uri.replaceAll("[^a-zA-Z0-9/]", "")
                                .replaceAll("/+", "_")
                                .replaceAll("^_|_$", "")
                                .toUpperCase();
                return method + "_" + cleanUri;
        }
}
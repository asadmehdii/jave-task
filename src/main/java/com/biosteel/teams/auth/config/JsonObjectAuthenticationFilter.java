package com.biosteel.teams.auth.config;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

import com.biosteel.teams.auth.dto.LoginCredentialsDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonObjectAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final ObjectMapper objectMapper;

    public JsonObjectAuthenticationFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        setRequiresAuthenticationRequestMatcher(
                new OrRequestMatcher(
                        new AntPathRequestMatcher("/api/users/login", "POST"),
                        new AntPathRequestMatcher("/users/login", "POST")));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        try {
            // Log the raw input stream for debugging
            String body = request.getReader().lines()
                    .reduce("", (accumulator, actual) -> accumulator + actual);
            log.debug("Received login request body: {}", body);

            LoginCredentialsDto credentials = objectMapper.readValue(body, LoginCredentialsDto.class);
            log.debug("Parsed credentials - username: {}", credentials.getUsername());

            if (credentials.getUsername() == null || credentials.getUsername().isEmpty()) {
                throw new IllegalArgumentException("Username cannot be empty");
            }
            if (credentials.getPassword() == null || credentials.getPassword().isEmpty()) {
                throw new IllegalArgumentException("Password cannot be empty");
            }

            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                    credentials.getUsername().trim(),
                    credentials.getPassword());
            setDetails(request, token);
            request.getSession().setAttribute("username", credentials.getUsername().trim());
            return this.getAuthenticationManager().authenticate(token);
        } catch (IOException e) {
            log.error("Failed to parse authentication request", e);
            throw new IllegalArgumentException("Failed to parse login request: " + e.getMessage());
        }
    }
}
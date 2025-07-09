package com.biosteel.teams.auth.config;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.biosteel.teams.auth.security.CustomPermissionEvaluator;
import com.biosteel.teams.auth.security.JwtAuthorizationFilter;
import com.biosteel.teams.auth.service.LoginAuthenticationSuccessHandler;
import com.biosteel.teams.user.model.User;
import com.biosteel.teams.user.security.DSUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Slf4j
public class SecurityConfig {

    private final JwtAuthorizationFilter jwtAuthorizationFilter;
    private final LoginAuthenticationSuccessHandler loginAuthenticationSuccessHandler;
    private final ObjectMapper objectMapper;
    private final AuthenticationConfiguration authenticationConfiguration;
    final private UserDetailsService userDetailsService;

    public SecurityConfig(
            JwtAuthorizationFilter jwtAuthorizationFilter,
            LoginAuthenticationSuccessHandler loginAuthenticationSuccessHandler,
            ObjectMapper objectMapper,
            AuthenticationConfiguration authenticationConfiguration,
            UserDetailsService userDetailsService) {
        this.jwtAuthorizationFilter = jwtAuthorizationFilter;
        this.loginAuthenticationSuccessHandler = loginAuthenticationSuccessHandler;
        this.objectMapper = objectMapper;
        this.authenticationConfiguration = authenticationConfiguration;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(new CustomPermissionEvaluator());
        return expressionHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        JsonObjectAuthenticationFilter authenticationFilter = new JsonObjectAuthenticationFilter(objectMapper);
        authenticationFilter.setAuthenticationSuccessHandler(loginAuthenticationSuccessHandler);
        authenticationFilter.setAuthenticationFailureHandler((request, response, exception) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            String errorMessage = "Authentication failed: " + exception.getMessage();

            String userId = "";
            if (exception instanceof DisabledException) {
                Object username = request.getSession().getAttribute("username");
                if (username != null) {
                    try {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username.toString());
                        if (userDetails instanceof DSUserDetails) {
                            User user = ((DSUserDetails) userDetails).getUser();
                            userId = user != null ? user.getUserId().toString() : "";
                        }
                    } catch (UsernameNotFoundException ex) {
                        log.debug("User not found while handling disabled account: " + username);
                    }
                }
            }
            objectMapper.writeValue(response.getWriter(),
                    Map.of("error", errorMessage,
                            "registrationId", userId));
        });
        authenticationFilter.setAuthenticationManager(authenticationConfiguration.getAuthenticationManager());

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterAt(authenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users/register/**", "/users/register/**").permitAll()
                        .requestMatchers("/api/users/invitation/**", "/users/invitation/**").permitAll()
                        .requestMatchers("/api/users/login", "/users/login").permitAll()
                        .requestMatchers("/api/users/*/avatar", "/users/*/avatar").permitAll()
                        .requestMatchers("/api/teams/*/avatar", "/teams/*/avatar").permitAll()
                        .requestMatchers("/api/users/resendForgotPasswordToken", "/users/resendForgotPasswordToken")
                        .permitAll()
                        .requestMatchers("/api/users/resetPassword/request", "/users/resetPassword/request").permitAll()
                        .requestMatchers("/api/users/resetPassword/confirm", "/users/resetPassword/confirm").permitAll()
                        .requestMatchers("/api/register/{registrationId}/verify/resend",
                                "/users/register/{registrationId}/verify/resend")
                        .permitAll()
                        .requestMatchers("/api/public/**", "/public/**").permitAll()
                        .requestMatchers("/api/v1/api-docs/**", "/v1/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}

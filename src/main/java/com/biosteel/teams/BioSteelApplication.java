package com.biosteel.teams;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
@EnableJpaRepositories(basePackages = {
        "com.biosteel.teams.user.repository",
        "com.biosteel.teams.role.repository",
        "com.biosteel.teams.auth.repository",
        "com.biosteel.teams.media.repository",
        "com.biosteel.teams.player.repository",
        "com.biosteel.teams.team.repository",
        "com.biosteel.teams.sport.repository",
        "com.biosteel.teams.game.repository",
        "com.biosteel.teams.practice.repository",
        "com.biosteel.teams.event.repository",
        "com.biosteel.teams.invitation.repository",
        "com.biosteel.teams.notification.repository",
        "com.biosteel.teams.audit.repository",
        "com.biosteel.teams.contact.repository",
        "com.biosteel.teams.advertisement.repository",
        "com.biosteel.teams.dashboard.repository"
})
@EntityScan(basePackages = {
        "com.biosteel.teams.user.model",
        "com.biosteel.teams.role.model",
        "com.biosteel.teams.auth.model",
        "com.biosteel.teams.media.model",
        "com.biosteel.teams.player.model",
        "com.biosteel.teams.team.model",
        "com.biosteel.teams.sport.model",
        "com.biosteel.teams.game.model",
        "com.biosteel.teams.practice.model",
        "com.biosteel.teams.event.model",
        "com.biosteel.teams.invitation.model",
        "com.biosteel.teams.notification.model",
        "com.biosteel.teams.audit.model",
        "com.biosteel.teams.contact.model",
        "com.biosteel.teams.advertisement.model"
})
@ComponentScan(basePackages = "com.biosteel.teams")
@EnableScheduling
@EnableAsync
public class BioSteelApplication {

    public static void main(String[] args) {
        log.debug("Starting BioSteel Teams...");

        // It's better to not log sensitive information like JWT_SECRET
        if (System.getenv("JWT_SECRET") == null) {
            log.warn("JWT_SECRET environment variable is not set!");
        }

        SpringApplication.run(BioSteelApplication.class, args);
        log.info("BioSteel Teams started successfully.");
    }
}
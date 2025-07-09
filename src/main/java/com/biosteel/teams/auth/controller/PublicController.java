package com.biosteel.teams.auth.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.biosteel.teams.common.dto.ApplicationInfoDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
@Tag(name = "Public APIs", description = "Publicly accessible APIs that don't require authentication")
public class PublicController {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.application.version}")
    private String applicationVersion;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @GetMapping("/hello")
    public String sayHello() {
        return "Hello, World!";
    }

    @GetMapping("/hello/{name}")
    public String sayHelloWithName(@PathVariable String name) {
        return String.format("Hello, %s!", name);
    }

    @Operation(summary = "Get application information", description = "Returns details about the application such as name, version, and status")
    @GetMapping("/application/info")
    public ApplicationInfoDto getApplicationInfo() {
        String status = "UP";

        return ApplicationInfoDto.builder()
                .name(applicationName)
                .version(applicationVersion)
                .status(status)
                .environment(activeProfile)
                .build();
    }
}
package com.biosteel.teams.user.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import com.biosteel.teams.role.dto.RoleDto;

import lombok.Data;

@Data
public class UserDto {
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private LocalDate dateOfBirth;
    private String gender;
    private UUID profilePhotoMediaId;
    private LocalDateTime registrationDate;
    private boolean enabled;
    private Set<RoleDto> roles;
    private LocalDateTime lastActivityDate;
}
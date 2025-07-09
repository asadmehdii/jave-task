package com.biosteel.teams.user.dto;

import java.time.LocalDate;
import java.util.UUID;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private LocalDate dateOfBirth;
    private String gender;
    private UUID profilePhotoMediaId;
    private String role;
}
package com.biosteel.teams.user.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class UserRegistrationResponse {
    private UUID registrationId;
    private String verificationCode;

}
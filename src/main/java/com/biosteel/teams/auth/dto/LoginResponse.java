package com.biosteel.teams.auth.dto;

import com.biosteel.teams.user.dto.UserDto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private UserDto user;
}
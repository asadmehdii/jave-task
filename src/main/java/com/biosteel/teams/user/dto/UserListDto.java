package com.biosteel.teams.user.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserListDto {
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private UUID profilePhotoMediaId;
}
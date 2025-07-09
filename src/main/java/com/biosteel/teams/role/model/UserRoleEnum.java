package com.biosteel.teams.role.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor

public enum UserRoleEnum {
    SUPER_ADMIN,
    ROLE_COACH,
    ROLE_USER,
    ROLE_MANAGER,
    ROLE_PLAYER
}

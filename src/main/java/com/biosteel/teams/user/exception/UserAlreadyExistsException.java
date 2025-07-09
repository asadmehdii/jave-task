package com.biosteel.teams.user.exception;

import com.biosteel.teams.common.exception.BioSteelException;

public class UserAlreadyExistsException extends BioSteelException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
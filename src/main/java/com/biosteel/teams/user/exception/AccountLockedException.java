package com.biosteel.teams.user.exception;

import com.biosteel.teams.common.exception.BioSteelException;

public class AccountLockedException extends BioSteelException {
    public AccountLockedException(String message) {
        super(message);
    }
}
package com.biosteel.teams.common.exception;

public abstract class BioSteelException extends RuntimeException {
    public BioSteelException(String message) {
        super(message);
    }

    public BioSteelException(String message, Throwable cause) {
        super(message, cause);
    }
}
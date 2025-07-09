package com.biosteel.teams.sport.service;

public class AttributeValidationException extends RuntimeException {
    private final String attributeCode;

    public AttributeValidationException(String attributeCode, String message) {
        super(String.format("Validation failed for attribute %s: %s", attributeCode, message));
        this.attributeCode = attributeCode;
    }

    public String getAttributeCode() {
        return attributeCode;
    }
}
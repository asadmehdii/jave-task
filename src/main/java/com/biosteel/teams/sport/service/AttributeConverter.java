package com.biosteel.teams.sport.service;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class AttributeConverter {
    private final ObjectMapper objectMapper;

    public String convertToString(Object value) {
        return value != null ? value.toString() : null;
    }

    public Double convertToNumber(Object value, String attributeCode) {
        if (value == null)
            return null;

        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            throw new AttributeValidationException(attributeCode, "Invalid number format");
        }
    }

    public Boolean convertToBoolean(Object value, String attributeCode) {
        if (value == null)
            return null;

        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        String strValue = value.toString().toLowerCase();
        if (strValue.equals("true") || strValue.equals("false")) {
            return Boolean.parseBoolean(strValue);
        }

        throw new AttributeValidationException(attributeCode, "Invalid boolean value");
    }
}
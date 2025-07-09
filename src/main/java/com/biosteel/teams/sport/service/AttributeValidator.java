package com.biosteel.teams.sport.service;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.biosteel.teams.sport.model.SportAttributeDefinition;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class AttributeValidator {
    private final ObjectMapper objectMapper;

    public void validate(SportAttributeDefinition definition, Object value) {
        validateRequired(definition, value);
        if (value != null) {
            validateByType(definition, value);
        }
    }

    private void validateRequired(SportAttributeDefinition definition, Object value) {
        if (definition.getIsRequired() && value == null) {
            throw new AttributeValidationException(definition.getCode(), "Value is required");
        }
    }

    private void validateByType(SportAttributeDefinition definition, Object value) {
        try {
            switch (definition.getDataType()) {
                case "ENUM":
                    validateEnum(definition, value.toString());
                    break;
                case "NUMBER":
                    validateNumber(definition, value);
                    break;
                case "BOOLEAN":
                    validateBoolean(value, definition.getCode());
                    break;
                case "STRING":
                    // No additional validation needed for strings
                    break;
                default:
                    throw new AttributeValidationException(definition.getCode(),
                            "Unsupported data type: " + definition.getDataType());
            }
        } catch (Exception e) {
            throw new AttributeValidationException(definition.getCode(), e.getMessage());
        }
    }

    private void validateEnum(SportAttributeDefinition definition, String value) {
        if (StringUtils.isEmpty(value)) {
            throw new AttributeValidationException(definition.getCode(), "Enum value cannot be empty");
        }

        try {
            JsonNode validationRules = objectMapper.readTree(definition.getValidationRules());
            JsonNode allowedValues = validationRules.get("values");

            if (allowedValues == null || allowedValues.isNull()) {
                throw new AttributeValidationException(definition.getCode(), "No allowed values defined for enum");
            }

            boolean isValid = StreamSupport.stream(allowedValues.spliterator(), false)
                    .anyMatch(jsonNode -> jsonNode.asText().equals(value));

            if (!isValid) {
                String validValues = StreamSupport.stream(allowedValues.spliterator(), false)
                        .map(JsonNode::asText)
                        .collect(Collectors.joining(", "));
                throw new AttributeValidationException(definition.getCode(),
                        String.format("Invalid enum value. Allowed values are: [%s]", validValues));
            }
        } catch (JsonProcessingException e) {
            throw new AttributeValidationException(definition.getCode(),
                    "Error parsing validation rules: " + e.getMessage());
        }
    }

    private void validateNumber(SportAttributeDefinition definition, Object value) {
        Double numericValue = parseNumber(value, definition.getCode());

        try {
            JsonNode validationRules = objectMapper.readTree(definition.getValidationRules());

            if (validationRules.has("min")) {
                double minValue = validationRules.get("min").asDouble();
                if (numericValue < minValue) {
                    throw new AttributeValidationException(definition.getCode(),
                            String.format("Value must be greater than or equal to %s", minValue));
                }
            }

            if (validationRules.has("max")) {
                double maxValue = validationRules.get("max").asDouble();
                if (numericValue > maxValue) {
                    throw new AttributeValidationException(definition.getCode(),
                            String.format("Value must be less than or equal to %s", maxValue));
                }
            }

            if (validationRules.has("step")) {
                double step = validationRules.get("step").asDouble();
                double remainder = numericValue % step;
                if (Math.abs(remainder) > 0.000001) { // Using small epsilon for floating point comparison
                    throw new AttributeValidationException(definition.getCode(),
                            String.format("Value must be a multiple of %s", step));
                }
            }
        } catch (JsonProcessingException e) {
            throw new AttributeValidationException(definition.getCode(),
                    "Error parsing validation rules: " + e.getMessage());
        }
    }

    private void validateBoolean(Object value, String attributeCode) {
        if (!(value instanceof Boolean) &&
                !value.toString().equalsIgnoreCase("true") &&
                !value.toString().equalsIgnoreCase("false")) {
            throw new AttributeValidationException(attributeCode,
                    "Value must be a boolean (true/false)");
        }
    }

    private Double parseNumber(Object value, String attributeCode) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            throw new AttributeValidationException(attributeCode,
                    "Value must be a valid number");
        }
    }
}
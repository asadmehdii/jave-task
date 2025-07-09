package com.biosteel.teams.user.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.Data;

@Data
public class UserRegistrationRequest {
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private String phone;

    private String gender;

    @NotBlank(message = "Password is required")
    private String password;

    private String role;

    @NotBlank(message = "Password confirmation is required")
    private String passwordConfirmation;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @AssertTrue(message = "Passwords must match")
    public boolean isPasswordsMatch() {
        if (password == null || passwordConfirmation == null) {
            return false;
        }
        return password.equals(passwordConfirmation);
    }
}
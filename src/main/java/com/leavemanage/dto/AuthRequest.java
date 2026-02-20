package com.leavemanage.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AuthRequest {
    @NotBlank

    @Email

    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@netpy\\.in$",
            message = "Only company email allowed"
    )
    private String email;
    @NotBlank
    @Pattern(
            regexp = "^(?=.*[^A-Za-z0-9]).{8,}$",
            message = "Password must be at least 8 characters long and contain at least one special character"
    )
    private String password;
}
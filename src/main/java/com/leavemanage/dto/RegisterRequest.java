package com.leavemanage.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;


@Data
public class RegisterRequest {
    @NotBlank
    private String name;
    @NotBlank

    @Email

    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@netpy\\.in$",
            message = "Registration allowed only with company email "
    )
    private String email;
    @NotBlank
    @Pattern(
            regexp = "^(?=.*[^A-Za-z0-9]).{8,}$",
            message = "Password must be at least 8 characters long and contain at least one special character"
    )
    private String password;
    private String role;
}

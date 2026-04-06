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
            regexp = "^[A-Za-z0-9._%+-]+@gmail\\.com$",
            message = "Only gmail is allowed"
    )
    private String email;
    @NotBlank
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$",
            message = "Password must meet the following requirements:\n"
            + "- At least 8 characters long\n"
            + "- At least one uppercase letter\n"
            + "- At least one lowercase letter\n"
            + "- At least one number\n"
            + "- At least one special character"
    )
    private String password;
}
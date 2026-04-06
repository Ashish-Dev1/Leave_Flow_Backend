package com.leavemanage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * DTO for change password requests.
 * All fields are validated to ensure security requirements are met.
 */
@Data
public class ChangePasswordRequestDTO {

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$",
            message ="Password must meet the following requirements:\n"
            + "- At least 8 characters long\n"
            + "- At least one uppercase letter\n"
            + "- At least one lowercase letter\n"
            + "- At least one number\n"
            + "- At least one special character"
    )
    
    private String newPassword;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
}

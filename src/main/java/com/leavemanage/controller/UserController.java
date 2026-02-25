package com.leavemanage.controller;

import com.leavemanage.dto.ChangePasswordRequestDTO;
import com.leavemanage.dto.LeaveDto;
import com.leavemanage.dto.RegisterResponse;
import com.leavemanage.model.LeaveRequest;
import com.leavemanage.model.User;
import com.leavemanage.service.PasswordService;
import com.leavemanage.service.UserService;
import com.leavemanage.util.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@Slf4j
@Tag(name = "User Management", description = "APIs for end users to manage their own leave requests and profile")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
        private final UserService userService;
        private final PasswordService passwordService;

        public UserController(UserService leaveService, PasswordService passwordService) {
            this.userService = leaveService;
            this.passwordService = passwordService;
        }

        @PostMapping("/leaves")
        @Operation(
                summary = "Apply for Leave",
                description = "Submit a new leave request. Frontend should send all required fields such as startDate, endDate, leaveType, and reason in JSON format. The created leave request will be returned."
        )
        @ApiResponses({
                @ApiResponse(
                        responseCode = "200",
                        description = "Leave request submitted successfully.",
                        content = @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = LeaveRequest.class),
                                examples = @ExampleObject(
                                        name = "CreateLeaveSuccess",
                                        summary = "Successful leave creation response",
                                        value = """
                                                {
                                                  "id": 1,
                                                  "startDate": "2026-02-28",
                                                  "endDate": "2026-03-02",
                                                  "leaveType": "ANNUAL",
                                                  "status": "PENDING",
                                                  "reason": "Family function",
                                                  "createdAt": "2026-02-25T10:30:00"
                                                }
                                                """
                                )
                        )
                ),
                @ApiResponse(
                        responseCode = "400",
                        description = "Bad Request - Missing or invalid fields in leave request.",
                        content = @Content(
                                mediaType = "application/json",
                                examples = @ExampleObject(
                                        name = "CreateLeaveBadRequest",
                                        summary = "Validation error example",
                                        value = """
                                                {
                                                  "timestamp": "2026-02-25T10:30:00",
                                                  "status": 400,
                                                  "error": "Bad Request",
                                                  "message": "startDate is required",
                                                  "path": "/api/v1/user/leaves"
                                                }
                                                """
                                )
                        )
                ),
                @ApiResponse(
                        responseCode = "500",
                        description = "Internal Server Error",
                        content = @Content(mediaType = "application/json")
                )
        })
        public LeaveRequest applyLeave(
                @io.swagger.v3.oas.annotations.parameters.RequestBody(
                        description = "Leave request payload. All fields are required unless marked optional.",
                        required = true,
                        content = @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = LeaveRequest.class),
                                examples = @ExampleObject(
                                        name = "CreateLeaveRequestExample",
                                        summary = "Create leave request example",
                                        value = """
                                                {
                                                  "startDate": "2026-02-28",
                                                  "endDate": "2026-03-02",
                                                  "leaveType": "ANNUAL",
                                                  "reason": "Family function"
                                                }
                                                """
                                )
                        )
                )
                @RequestBody LeaveRequest leave,
                Authentication auth) {

            CustomUserDetails userDetails =
                    (CustomUserDetails) auth.getPrincipal();

            User user = userDetails.getUser();

            return userService.applyLeave(leave, user);

        }

        @GetMapping("/leaves/my")
        @Operation(
                summary = "Get My Leaves",
                description = "Retrieve all leave requests for the currently authenticated user. No request body required; the backend resolves the user from the JWT token."
        )
        @ApiResponses({
                @ApiResponse(
                        responseCode = "200",
                        description = "List of leave requests for the current user.",
                        content = @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = LeaveDto.class)
                        )
                ),
                @ApiResponse(
                        responseCode = "401",
                        description = "Unauthorized - Missing or invalid JWT token.",
                        content = @Content(mediaType = "application/json")
                ),
                @ApiResponse(
                        responseCode = "500",
                        description = "Internal Server Error",
                        content = @Content(mediaType = "application/json")
                )
        })
        public List<LeaveDto> myLeaves(Authentication auth) {

            CustomUserDetails userDetails =
                    (CustomUserDetails) auth.getPrincipal();

            User user = userDetails.getUser();
            return userService.getMyLeaves(user.getId());
        }

        @PutMapping("/profile")
        @Operation(
                summary = "Update Profile",
                description = "Update the authenticated user's profile information such as name, email, or other editable fields. The full updated user entity is returned."
        )
        @ApiResponses({
                @ApiResponse(
                        responseCode = "200",
                        description = "Profile updated successfully.",
                        content = @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = User.class)
                        )
                ),
                @ApiResponse(
                        responseCode = "400",
                        description = "Bad Request - Invalid profile data.",
                        content = @Content(mediaType = "application/json")
                ),
                @ApiResponse(
                        responseCode = "401",
                        description = "Unauthorized - Missing or invalid JWT token.",
                        content = @Content(mediaType = "application/json")
                ),
                @ApiResponse(
                        responseCode = "500",
                        description = "Internal Server Error",
                        content = @Content(mediaType = "application/json")
                )
        })
        public User updateProfile(
                @io.swagger.v3.oas.annotations.parameters.RequestBody(
                        description = "User profile data to update. Only editable fields should be sent from the frontend.",
                        required = true,
                        content = @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = User.class),
                                examples = @ExampleObject(
                                        name = "UpdateProfileRequestExample",
                                        summary = "Update profile details",
                                        value = """
                                                {
                                                  "name": "John Doe",
                                                  "email": "john@example.com"
                                                }
                                                """
                                )
                        )
                )
                @RequestBody User profileData,
                Authentication auth) {
            
            CustomUserDetails userDetails =
                    (CustomUserDetails) auth.getPrincipal();

            User currentUser = userDetails.getUser();
            return userService.updateProfile(currentUser.getId(), profileData);
        }

        @PatchMapping("/change-password")
        @PreAuthorize("isAuthenticated()")
        @Operation(
                summary = "Change Password",
                description = "Change the authenticated user's password. Frontend must send the current password and new password. All existing tokens will be invalidated."
        )
        @ApiResponses({
                @ApiResponse(
                        responseCode = "200",
                        description = "Password changed successfully.",
                        content = @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = RegisterResponse.class),
                                examples = @ExampleObject(
                                        name = "ChangePasswordSuccess",
                                        summary = "Password change confirmation",
                                        value = """
                                                {
                                                  "message": "Password changed successfully. Please log in again with your new password."
                                                }
                                                """
                                )
                        )
                ),
                @ApiResponse(
                        responseCode = "400",
                        description = "Bad Request - Validation failed (e.g., weak password, passwords do not match).",
                        content = @Content(mediaType = "application/json")
                ),
                @ApiResponse(
                        responseCode = "401",
                        description = "Unauthorized - Missing or invalid JWT token.",
                        content = @Content(mediaType = "application/json")
                ),
                @ApiResponse(
                        responseCode = "500",
                        description = "Internal Server Error",
                        content = @Content(mediaType = "application/json")
                )
        })
        public ResponseEntity<RegisterResponse> changePassword(
                Principal principal,
                @Valid
                @io.swagger.v3.oas.annotations.parameters.RequestBody(
                        description = "Password change payload including current password, new password, and confirmation.",
                        required = true,
                        content = @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = ChangePasswordRequestDTO.class),
                                examples = @ExampleObject(
                                        name = "ChangePasswordRequestExample",
                                        summary = "Change password request",
                                        value = """
                                                {
                                                  "currentPassword": "OldPassword123",
                                                  "newPassword": "NewStrongPassword123",
                                                  "confirmNewPassword": "NewStrongPassword123"
                                                }
                                                """
                                )
                        )
                )
                @RequestBody ChangePasswordRequestDTO request) {

            log.info("Received password change request");

            passwordService.changePassword(principal, request);

            return ResponseEntity.ok(new RegisterResponse("Password changed successfully. Please log in again with your new password."));
        }
    }

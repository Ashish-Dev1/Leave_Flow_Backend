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
@Tag(name = "User", description = "User APIs for managing own leaves and profile")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
        private final UserService userService;
        private final PasswordService passwordService;

        public UserController(UserService leaveService, PasswordService passwordService) {
            this.userService = leaveService;
            this.passwordService = passwordService;
        }

        @PostMapping("/leaves")
        @Operation(summary = "Apply for Leave", description = "Submit a new leave request")
        public LeaveRequest applyLeave(
                @RequestBody LeaveRequest leave,
                Authentication auth) {

            CustomUserDetails userDetails =
                    (CustomUserDetails) auth.getPrincipal();

            User user = userDetails.getUser();

            return userService.applyLeave(leave, user);

        }

        @GetMapping("/leaves/my")
        @Operation(summary = "Get My Leaves", description = "Retrieve all leave requests for the authenticated user")
        public List<LeaveDto> myLeaves(Authentication auth) {

            CustomUserDetails userDetails =
                    (CustomUserDetails) auth.getPrincipal();

            User user = userDetails.getUser();
            return userService.getMyLeaves(user.getId());
        }

        @PutMapping("/profile")
        @Operation(summary = "Update Profile", description = "Update the authenticated user's profile information")
        public User updateProfile(
                @RequestBody User profileData,
                Authentication auth) {
            
            CustomUserDetails userDetails =
                    (CustomUserDetails) auth.getPrincipal();

            User currentUser = userDetails.getUser();
            return userService.updateProfile(currentUser.getId(), profileData);
        }

        @PatchMapping("/change-password")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Change Password", description = "Change the authenticated user's password. Invalidates all existing tokens.")
        public ResponseEntity<RegisterResponse> changePassword(
                Principal principal,
                @Valid @RequestBody ChangePasswordRequestDTO request) {

            log.info("Received password change request");

            passwordService.changePassword(principal, request);

            return ResponseEntity.ok(new RegisterResponse("Password changed successfully. Please log in again with your new password."));
        }
    }

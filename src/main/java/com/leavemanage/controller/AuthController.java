package com.leavemanage.controller;

import com.leavemanage.dto.AuthRequest;
import com.leavemanage.dto.AuthResponse;
import com.leavemanage.dto.RegisterRequest;
import com.leavemanage.dto.RegisterResponse;
import com.leavemanage.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication and authorization APIs for user login and registration")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "User Login", description = "Authenticate user with email and password, returns JWT token")
    public ResponseEntity<?> login(@Valid  @RequestBody AuthRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new RegisterResponse("Invalid credentials: " + e.getMessage()));
        }
    }

    @PostMapping("/register")
    @Operation(summary = "User Registration", description = "Register a new user with email, password, name, and department")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            authService.register(request);
            return ResponseEntity.ok(new RegisterResponse("User registered successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new RegisterResponse("Registration failed: " + e.getMessage()));
        }
    }
}



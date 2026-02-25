package com.leavemanage.controller;

import com.leavemanage.dto.AuthRequest;
import com.leavemanage.dto.AuthResponse;
import com.leavemanage.dto.RegisterRequest;
import com.leavemanage.dto.RegisterResponse;
import com.leavemanage.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(
            summary = "User Login",
            description = "Authenticate user with email and password. Frontend must send JSON with the user's credentials and receives a JWT access token on success.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User credentials for login. All fields are required.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthRequest.class),
                            examples = @ExampleObject(
                                    name = "LoginRequestExample",
                                    summary = "Basic login request",
                                    value = """
                                            {
                                              "email": "john@example.com",
                                              "password": "StrongPassword123"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful. Returns JWT token and user details.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(
                                    name = "LoginSuccessResponse",
                                    summary = "Successful login response",
                                    value = """
                                            {
                                              "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                              "email": "john@example.com",
                                              "roles": ["USER"],
                                              "expiresAt": "2026-02-25T10:30:00"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid payload or missing required fields.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "LoginBadRequest",
                                    summary = "Validation error example",
                                    value = """
                                            {
                                              "timestamp": "2026-02-25T10:30:00",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Email is required",
                                              "path": "/api/auth/login"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid credentials.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegisterResponse.class),
                            examples = @ExampleObject(
                                    name = "LoginUnauthorized",
                                    summary = "Invalid credentials example",
                                    value = """
                                            {
                                              "message": "Invalid credentials: Bad credentials"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "LoginServerError",
                                    summary = "Unexpected error example",
                                    value = """
                                            {
                                              "timestamp": "2026-02-25T10:30:00",
                                              "status": 500,
                                              "error": "Internal Server Error",
                                              "message": "Unexpected error occurred while processing login request",
                                              "path": "/api/auth/login"
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new RegisterResponse("Invalid credentials: " + e.getMessage()));
        }
    }

    @PostMapping("/register")
    @Operation(
            summary = "User Registration",
            description = "Register a new user with email, password, name, and department. Frontend must send all required fields in JSON format and receives a confirmation message on success.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User registration details. All fields are required unless marked as optional.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegisterRequest.class),
                            examples = @ExampleObject(
                                    name = "RegisterRequestExample",
                                    summary = "Basic user registration",
                                    value = """
                                            {
                                              "name": "John Doe",
                                              "email": "john@example.com",
                                              "password": "StrongPassword123",
                                              "department": "Engineering"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegisterResponse.class),
                            examples = @ExampleObject(
                                    name = "RegisterSuccessResponse",
                                    summary = "Successful registration response",
                                    value = """
                                            {
                                              "message": "User registered successfully"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Validation failed or user already exists.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegisterResponse.class),
                            examples = @ExampleObject(
                                    name = "RegisterBadRequest",
                                    summary = "Registration error example",
                                    value = """
                                            {
                                              "message": "Registration failed: Email already in use"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "RegisterServerError",
                                    summary = "Unexpected error example",
                                    value = """
                                            {
                                              "timestamp": "2026-02-25T10:30:00",
                                              "status": 500,
                                              "error": "Internal Server Error",
                                              "message": "Unexpected error occurred while processing registration request",
                                              "path": "/api/auth/register"
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new RegisterResponse("User registered successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new RegisterResponse("Registration failed: " + e.getMessage()));
        }
    }
}



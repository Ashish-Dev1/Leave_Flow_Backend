package com.leavemanage.service;

import com.leavemanage.dto.ChangePasswordRequestDTO;
import com.leavemanage.exception.*;
import com.leavemanage.model.User;
import com.leavemanage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Service responsible for password change operations.
 * Handles all business logic, validation, and security checks.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Password strength pattern: min 8 chars, at least 1 special character
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[^A-Za-z0-9]).{8,}$");

    /**
     * Changes the password for the authenticated user.
     * This method is thread-safe through @Transactional and database locking.
     *
     * @param principal the authenticated user's principal
     * @param request   the password change request containing current, new, and confirm passwords
     * @throws UserNotAuthenticatedException if principal is null or user email is blank
     * @throws UserNotFoundException         if user cannot be found in database
     * @throws IncorrectPasswordException    if current password is incorrect
     * @throws PasswordsMismatchException    if new password and confirm password don't match
     * @throws SamePasswordException         if new password is same as current password
     * @throws PasswordValidationException   if new password fails strength requirements
     */
    @Transactional
    public void changePassword(Principal principal, ChangePasswordRequestDTO request) {
        // Validate principal and authentication
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new UserNotAuthenticatedException("User is not authenticated. Please log in.");
        }

        String userEmail = principal.getName();
        log.info("Processing password change request for user: {}", userEmail);

        // Fetch user with pessimistic lock to handle concurrent requests safely
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + userEmail));

        // Validate current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            log.warn("Incorrect current password attempt for user: {}", userEmail);
            throw new IncorrectPasswordException("Current password is incorrect");
        }

        // Validate new password and confirm password match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new PasswordsMismatchException("New password and confirm password do not match");
        }

        // Validate new password is not same as current password
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new SamePasswordException("New password cannot be the same as the current password");
        }

        // Validate password strength (additional server-side check beyond DTO validation)
        if (!PASSWORD_PATTERN.matcher(request.getNewPassword()).matches()) {
            throw new PasswordValidationException("Password must be at least 8 characters long and contain at least one special character");
        }

        // Encode and set new password
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(encodedPassword);

        // Update password changed timestamp for JWT invalidation
        // This ensures all tokens issued before this timestamp become invalid
        user.setPasswordChangedAt(LocalDateTime.now());

        // Save user (thread-safe due to @Transactional)
        userRepository.save(user);

        log.info("Password successfully changed for user: {}", userEmail);
    }

    /**
     * Checks if a JWT token was issued before the user's last password change.
     * Used to invalidate tokens after password change for security.
     *
     * @param tokenIssuedAt the timestamp when the JWT was issued
     * @param userEmail     the user's email to check against
     * @return true if token is valid (issued after last password change), false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isTokenValidAfterPasswordChange(Date tokenIssuedAt, String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElse(null);

        if (user == null || user.getPasswordChangedAt() == null) {
            // User not found or password never changed - token is valid
            return true;
        }

        // Convert LocalDateTime to Date for comparison
        LocalDateTime passwordChangedAt = user.getPasswordChangedAt();
        Date passwordChangedDate = Date.from(passwordChangedAt.atZone(ZoneId.systemDefault()).toInstant());

        // Token is valid only if issued AFTER password change
        return tokenIssuedAt.after(passwordChangedDate);
    }
}

package com.leavemanage.exception;

/**
 * Exception thrown when password validation fails (e.g., strength requirements).
 */
public class PasswordValidationException extends RuntimeException {
    public PasswordValidationException(String message) {
        super(message);
    }
}

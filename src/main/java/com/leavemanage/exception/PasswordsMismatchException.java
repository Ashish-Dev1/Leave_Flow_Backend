package com.leavemanage.exception;

/**
 * Exception thrown when new password and confirm password do not match.
 */
public class PasswordsMismatchException extends RuntimeException {
    public PasswordsMismatchException(String message) {
        super(message);
    }
}

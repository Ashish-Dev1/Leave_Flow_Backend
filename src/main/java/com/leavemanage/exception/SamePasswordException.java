package com.leavemanage.exception;

/**
 * Exception thrown when the new password is the same as the current password.
 */
public class SamePasswordException extends RuntimeException {
    public SamePasswordException(String message) {
        super(message);
    }
}

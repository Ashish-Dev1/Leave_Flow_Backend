package com.leavemanage.exception;

/**
 * Exception thrown when the user is not authenticated or principal is null.
 */
public class UserNotAuthenticatedException extends RuntimeException {
    public UserNotAuthenticatedException(String message) {
        super(message);
    }
}

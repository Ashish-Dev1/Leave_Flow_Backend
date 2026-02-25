package com.leavemanage.exception;

/**
 * Exception thrown when the current password provided is incorrect.
 */
public class IncorrectPasswordException extends RuntimeException {
    public IncorrectPasswordException(String message) {
        super(message);
    }
}

package com.leavemanage.exception;

import com.leavemanage.dto.RegisterResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RegisterResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = "Validation failed";
        FieldError fieldError = ex.getBindingResult().getFieldError();
        if (fieldError != null && fieldError.getDefaultMessage() != null) {
            message = fieldError.getDefaultMessage();
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new RegisterResponse(message));
    }

    // 400 Bad Request - Client errors
    @ExceptionHandler({
            IncorrectPasswordException.class,
            PasswordsMismatchException.class,
            SamePasswordException.class,
            PasswordValidationException.class
    })
    public ResponseEntity<RegisterResponse> handleBadRequestExceptions(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new RegisterResponse(ex.getMessage()));
    }

    // 401 Unauthorized - Authentication required
    @ExceptionHandler(UserNotAuthenticatedException.class)
    public ResponseEntity<RegisterResponse> handleUnauthorized(UserNotAuthenticatedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new RegisterResponse(ex.getMessage()));
    }

    // 404 Not Found - Resource not found
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<RegisterResponse> handleNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new RegisterResponse(ex.getMessage()));
    }

    // 500 Internal Server Error - Unexpected errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RegisterResponse> handleGenericException(Exception ex) {
        // Log the full stack trace for debugging (in production, use proper logging)
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new RegisterResponse("An unexpected error occurred. Please try again later."));
    }
}

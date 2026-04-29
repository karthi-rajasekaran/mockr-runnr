package com.mockr.runnr.exception;

/**
 * Exception thrown when validation fails.
 * Should return 400 Bad Request.
 */
public class ValidationException extends ApplicationException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.mockr.runnr.exception;

/**
 * Base exception for application-level errors.
 * Used for domain-specific errors that should return 400 Bad Request.
 */
public class ApplicationException extends RuntimeException {

    public ApplicationException(String message) {
        super(message);
    }

    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}

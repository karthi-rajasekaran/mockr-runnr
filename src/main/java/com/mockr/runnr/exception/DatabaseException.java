package com.mockr.runnr.exception;

/**
 * Exception thrown when database operations fail.
 * Should return 503 Service Unavailable.
 */
public class DatabaseException extends ApplicationException {

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}

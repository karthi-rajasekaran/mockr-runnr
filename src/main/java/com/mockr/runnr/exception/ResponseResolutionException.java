package com.mockr.runnr.exception;

/**
 * Exception thrown when response resolution fails.
 * This is a runtime exception for responses that cannot be resolved during
 * request handling.
 */
public class ResponseResolutionException extends RuntimeException {

    public ResponseResolutionException(String message) {
        super(message);
    }

    public ResponseResolutionException(String message, Throwable cause) {
        super(message, cause);
    }
}

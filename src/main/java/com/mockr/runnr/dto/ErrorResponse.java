package com.mockr.runnr.dto;

/**
 * Structured error response returned to clients.
 * Contains status code, error message, timestamp, and request path.
 * Timestamp is in milliseconds since Unix epoch.
 */
public record ErrorResponse(
        int statusCode,
        String message,
        String error,
        long timestamp,
        String path) {
}

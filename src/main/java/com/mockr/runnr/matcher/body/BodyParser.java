package com.mockr.runnr.matcher.body;

import java.util.Map;

/**
 * Strategy interface for parsing request bodies in various formats.
 * 
 * Implementations return flattened map of fields with no prefix.
 * Caller adds "body." prefix when building unified field map.
 */
public interface BodyParser {

    /**
     * Parse body into flat map of fields.
     * 
     * @param body Raw body string
     * @return Map of flattened fields (no prefix)
     * @throws BodyParseException if parsing fails
     */
    Map<String, Object> parse(String body) throws BodyParseException;

    /**
     * Exception thrown when body parsing fails.
     */
    class BodyParseException extends Exception {
        public BodyParseException(String message) {
            super(message);
        }

        public BodyParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

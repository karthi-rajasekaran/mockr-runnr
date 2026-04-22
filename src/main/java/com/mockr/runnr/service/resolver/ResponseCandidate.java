package com.mockr.runnr.service.resolver;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Immutable DTO wrapping a response candidate with its evaluation score.
 * Used during response selection to hold scoring metadata for tie-breaking.
 */
public record ResponseCandidate(
        UUID responseId,
        Integer statusCode,
        Map<String, String> headers,
        String body,
        String responseType,
        LocalDateTime createdAt,
        Integer score // = number of matched conditions
) {
    /**
     * Score represents the specificity of the response.
     * Score = number of conditions that matched the request.
     * Higher score = more specific response = higher priority.
     */
}

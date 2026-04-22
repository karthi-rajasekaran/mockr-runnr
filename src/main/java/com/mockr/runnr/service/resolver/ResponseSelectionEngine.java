package com.mockr.runnr.service.resolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Selection engine for smart response resolution.
 * Implements 3-tier tie-breaking logic to deterministically select
 * the best response from multiple candidates.
 */
@Component
public class ResponseSelectionEngine {
    private static final Logger logger = LoggerFactory.getLogger(ResponseSelectionEngine.class);

    /**
     * Find the best matching response from candidates using deterministic
     * tie-breaking.
     *
     * Sorting order (deterministic):
     * 1. Score (descending) - highest specificity first
     * 2. CreatedAt (descending) - latest first (most recent)
     * 3. ResponseId (descending) - highest ID first (final deterministic
     * tiebreaker)
     *
     * @param candidates List of candidate responses to evaluate
     * @return Optional containing the best matching response, or empty if no
     *         candidates
     */
    public Optional<ResponseCandidate> findBestMatch(List<ResponseCandidate> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            logger.debug("No candidates to select from");
            return Optional.empty();
        }

        return candidates.stream()
                .sorted(Comparator
                        .comparingInt(ResponseCandidate::score).reversed() // Tier 1: Highest score first (most
                                                                           // specific)
                        .thenComparing(ResponseCandidate::createdAt).reversed() // Tier 2: Latest createdAt first
                        .thenComparing(ResponseCandidate::responseId).reversed() // Tier 3: Highest responseId first
                                                                                 // (deterministic)
                )
                .peek(best -> logger.debug("Selected best response: id={}, score={}, createdAt={}",
                        best.responseId(), best.score(), best.createdAt()))
                .findFirst();
    }

    /**
     * Calculate score for a response based on number of matched conditions.
     * Score = condition count (higher = more specific).
     *
     * @param matchedConditionCount Number of conditions that matched
     * @return Calculated score
     */
    public int calculateScore(int matchedConditionCount) {
        return matchedConditionCount;
    }
}

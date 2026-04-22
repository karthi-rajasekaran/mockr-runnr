package com.mockr.runnr.matcher;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.util.regex.Pattern;

/**
 * PathMatcher - Matches incoming request paths against configured endpoint
 * patterns.
 * 
 * Features:
 * - Exact path matching: /api/user matches /api/user
 * - Path parameter matching: /api/user/{user-id} matches /api/user/1234
 * - URI variable pattern: {var-name} syntax
 * 
 * Matching precedence (highest to lowest):
 * 1. Exact matches: /api/user == /api/user
 * 2. Parameterized matches: /api/user/{user-id} ~= /api/user/1234
 * 
 * Performance:
 * - O(1) exact path comparison
 * - O(n) pattern compilation and matching (cached patterns recommended for
 * callers)
 * - No regex compilation on every match
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PathMatcher {

    /**
     * Check if an incoming request path matches a configured endpoint pattern.
     *
     * @param configuredPattern Configured pattern (e.g., /api/user/{user-id})
     * @param requestPath       Incoming request path (e.g., /api/user/1234)
     * @return true if paths match exactly or by pattern
     */
    public static boolean matches(String configuredPattern, String requestPath) {
        if (configuredPattern == null || requestPath == null) {
            return false;
        }

        // Exact match takes precedence
        if (configuredPattern.equals(requestPath)) {
            return true;
        }

        // Try parameterized pattern matching
        return matchesPattern(configuredPattern, requestPath);
    }

    /**
     * Check if request path matches a parameterized pattern.
     * Pattern syntax: /api/user/{user-id}
     * Matches: /api/user/1234, /api/user/john-doe, etc.
     *
     * Trailing slash behavior: Must match exactly
     * - /api/user matches /api/user (same format)
     * - /api/user/ matches /api/user/ (same format)
     * - /api/user does NOT match /api/user/ (different format)
     *
     * Algorithm:
     * 1. Split both pattern and request path by / (preserving trailing empty
     * segments)
     * 2. Must have same segment count
     * 3. For each segment: exact match OR pattern segment (validate parameter
     * format)
     *
     * @param pattern     Configured pattern with potential {var} placeholders
     * @param requestPath Request path to match against pattern
     * @return true if paths match pattern semantics with same trailing slash format
     */
    private static boolean matchesPattern(String pattern, String requestPath) {
        String[] patternSegments = pattern.split("/", -1);
        String[] requestSegments = requestPath.split("/", -1);

        // Different segment counts = no match (includes trailing slash differences)
        if (patternSegments.length != requestSegments.length) {
            return false;
        }

        // Check each segment
        for (int i = 0; i < patternSegments.length; i++) {
            String patternSegment = patternSegments[i];
            String requestSegment = requestSegments[i];

            // Exact segment match
            if (patternSegment.equals(requestSegment)) {
                continue;
            }

            // Check if pattern segment is a parameter placeholder {var-name}
            if (isParameterPlaceholder(patternSegment)) {
                // Validate request segment is non-empty (no empty path segments)
                if (requestSegment.isEmpty()) {
                    return false;
                }
                // Parameter matches any non-empty segment
                continue;
            }

            // Segment doesn't match exactly and pattern segment is not a param
            return false;
        }

        return true;
    }

    /**
     * Check if a path segment is a parameter placeholder.
     * Parameter placeholders are in format: {variable-name}
     * 
     * Valid examples: {id}, {user-id}, {resource_name}
     *
     * @param segment Path segment to check
     * @return true if segment is a parameter placeholder
     */
    private static boolean isParameterPlaceholder(String segment) {
        return segment.startsWith("{") && segment.endsWith("}") && segment.length() > 2;
    }

    /**
     * Determine match type for logging and precedence ordering.
     * 
     * @param configuredPattern Configured endpoint pattern
     * @param requestPath       Request path
     * @return MatchType enum indicating type of match
     */
    public static MatchType getMatchType(String configuredPattern, String requestPath) {
        if (configuredPattern == null || requestPath == null) {
            return MatchType.NONE;
        }

        if (configuredPattern.equals(requestPath)) {
            return MatchType.EXACT;
        }

        if (matchesPattern(configuredPattern, requestPath)) {
            return MatchType.PARAMETRIZED;
        }

        return MatchType.NONE;
    }

    /**
     * Types of path matches for precedence ordering.
     */
    public enum MatchType {
        EXACT(0), // Highest precedence
        PARAMETRIZED(1), // Lower precedence
        NONE(2); // No match

        private final int priority;

        MatchType(int priority) {
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }

        public boolean isMatch() {
            return this != NONE;
        }
    }
}

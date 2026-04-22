package com.mockr.runnr.matcher;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PathMatcher - Path Pattern Matching Tests")
class PathMatcherTest {

    @Test
    @DisplayName("Should match exact paths")
    void shouldMatchExactPaths() {
        assertTrue(PathMatcher.matches("/api/user", "/api/user"));
        assertTrue(PathMatcher.matches("/api/users", "/api/users"));
        assertTrue(PathMatcher.matches("/", "/"));
        assertTrue(PathMatcher.matches("/api/v1/users/search", "/api/v1/users/search"));
    }

    @Test
    @DisplayName("Should not match different exact paths")
    void shouldNotMatchDifferentPaths() {
        assertFalse(PathMatcher.matches("/api/user", "/api/users"));
        assertFalse(PathMatcher.matches("/api/user", "/api/user/"));
        assertFalse(PathMatcher.matches("/users", "/api/users"));
    }

    @Test
    @DisplayName("Should match parameterized paths")
    void shouldMatchParametrizedPaths() {
        // Single parameter
        assertTrue(PathMatcher.matches("/api/user/{user-id}", "/api/user/1234"));
        assertTrue(PathMatcher.matches("/api/user/{user-id}", "/api/user/john-doe"));
        assertTrue(PathMatcher.matches("/api/user/{user-id}", "/api/user/abc123_xyz"));

        // Multiple parameters
        assertTrue(PathMatcher.matches("/api/user/{user-id}/posts/{post-id}", "/api/user/1234/posts/5678"));
        assertTrue(PathMatcher.matches("/api/{resource}/{id}", "/api/users/123"));

        // Parameters at different positions
        assertTrue(PathMatcher.matches("/{version}/api/user/{id}", "/v1/api/user/123"));
    }

    @Test
    @DisplayName("Should not match parameterized pattern with wrong segment count")
    void shouldNotMatchWrongSegmentCount() {
        assertFalse(PathMatcher.matches("/api/user/{user-id}", "/api/user"));
        assertFalse(PathMatcher.matches("/api/user/{user-id}", "/api/user/123/extra"));
        assertFalse(PathMatcher.matches("/api/{id}", "/api/user/123"));
    }

    @Test
    @DisplayName("Should not match parameterized pattern with empty segments")
    void shouldNotMatchEmptySegments() {
        assertFalse(PathMatcher.matches("/api/user/{user-id}", "/api/user/"));
        assertFalse(PathMatcher.matches("/api/{id}/posts", "/api//posts"));
    }

    @Test
    @DisplayName("Should handle trailing slashes correctly")
    void shouldHandleTrailingSlashes() {
        // Same trailing slash behavior
        assertTrue(PathMatcher.matches("/api/user/", "/api/user/"));
        assertFalse(PathMatcher.matches("/api/user", "/api/user/"));
        assertFalse(PathMatcher.matches("/api/user/", "/api/user"));
    }

    @Test
    @DisplayName("Should handle root path")
    void shouldHandleRootPath() {
        assertTrue(PathMatcher.matches("/", "/"));
        assertFalse(PathMatcher.matches("/", "/api"));
        assertFalse(PathMatcher.matches("", "/"));
    }

    @Test
    @DisplayName("Should handle null inputs safely")
    void shouldHandleNullInputs() {
        assertFalse(PathMatcher.matches(null, "/api/user"));
        assertFalse(PathMatcher.matches("/api/user", null));
        assertFalse(PathMatcher.matches(null, null));
    }

    @ParameterizedTest
    @DisplayName("Should correctly identify match types")
    @CsvSource({
            "/api/user, /api/user, EXACT",
            "/api/user/{id}, /api/user/123, PARAMETRIZED",
            "/api/user, /api/users, NONE",
            "/api/{id}, /api/user/123, NONE"
    })
    void shouldIdentifyMatchTypes(String pattern, String path, String expectedType) {
        PathMatcher.MatchType expected = PathMatcher.MatchType.valueOf(expectedType);
        assertEquals(expected, PathMatcher.getMatchType(pattern, path));
    }

    @Test
    @DisplayName("Should prioritize exact matches over parameterized matches")
    void shouldPrioritizeExactMatches() {
        PathMatcher.MatchType exact = PathMatcher.getMatchType("/api/user", "/api/user");
        PathMatcher.MatchType parametrized = PathMatcher.getMatchType("/api/{id}", "/api/user");

        assertTrue(exact.getPriority() < parametrized.getPriority(),
                "Exact match priority should be lower than parametrized");
    }

    @Test
    @DisplayName("Should handle complex real-world paths")
    void shouldHandleComplexPaths() {
        // RESTful patterns
        assertTrue(PathMatcher.matches("/api/v1/users/{userId}/posts/{postId}/comments/{commentId}",
                "/api/v1/users/123/posts/456/comments/789"));

        // Query-like patterns
        assertTrue(PathMatcher.matches("/search/{query}", "/search/java-spring-boot"));

        // UUID patterns
        assertTrue(PathMatcher.matches("/api/resource/{id}",
                "/api/resource/550e8400-e29b-41d4-a716-446655440000"));
    }

    @Test
    @DisplayName("Parameter placeholder should contain variable names")
    void parameterPlaceholderValidation() {
        assertTrue(PathMatcher.matches("/api/{id}", "/api/123"));
        assertTrue(PathMatcher.matches("/api/{user-id}", "/api/123"));
        assertTrue(PathMatcher.matches("/api/{user_id}", "/api/123"));
        assertTrue(PathMatcher.matches("/api/{userId}", "/api/123"));
    }

    @Test
    @DisplayName("Should reject malformed parameter patterns")
    void shouldRejectMalformedPatterns() {
        // Missing closing brace
        assertFalse(PathMatcher.matches("/api/{id", "/api/123"));

        // Missing opening brace
        assertFalse(PathMatcher.matches("/api/id}", "/api/123"));

        // Empty braces
        assertFalse(PathMatcher.matches("/api/{}", "/api/123"));
    }
}

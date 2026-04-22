package com.mockr.runnr.matcher;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PathMatcherDebugTest {

    @Test
    void debugTrailingSlashMismatch() {
        String configuredPattern = "/api/user";
        String requestPath = "/api/user/";

        // Debug: Print split results
        String[] patternSegments = configuredPattern.split("/");
        String[] requestSegments = requestPath.split("/");

        System.out.println("Pattern: " + configuredPattern);
        System.out.println(
                "Pattern segments (" + patternSegments.length + "): " + java.util.Arrays.toString(patternSegments));
        System.out.println("Request: " + requestPath);
        System.out.println(
                "Request segments (" + requestSegments.length + "): " + java.util.Arrays.toString(requestSegments));

        boolean result = PathMatcher.matches(configuredPattern, requestPath);
        System.out.println("Matches result: " + result);

        // This should be false because trailing slashes differ
        assertFalse(result, "Paths with different trailing slash format should not match");
    }

    @Test
    void debugExactMatchesSameTrailingSlash() {
        // Both have trailing slash
        assertTrue(PathMatcher.matches("/api/user/", "/api/user/"));

        // Both without trailing slash
        assertTrue(PathMatcher.matches("/api/user", "/api/user"));
    }
}

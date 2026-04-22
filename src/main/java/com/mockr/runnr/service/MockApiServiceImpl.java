package com.mockr.runnr.service;

import com.mockr.runnr.domain.Endpoint;
import com.mockr.runnr.dto.MockApiResponse;
import com.mockr.runnr.dto.MockRequest;
import com.mockr.runnr.exception.ResponseResolutionException;
import com.mockr.runnr.matcher.PathMatcher;
import com.mockr.runnr.service.resolver.ResponseResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * MockApiServiceImpl - MVP implementation of MockApiService.
 * 
 * Core orchestration logic:
 * 1. Extract project ID from request context
 * 2. Load all endpoints for the project from cache
 * 3. Find matching endpoint by path + method with smart path matching
 * 4. Use ResponseResolver to resolve the best matching response
 * 5. Convert ResponseEntity to MockApiResponse with proper headers
 * 6. Return 404 if endpoint or response not found
 * 
 * Path Matching Strategy (Issue 6 - PathMatcher):
 * - Exact path match: /api/user matches /api/user (highest precedence)
 * - Parameterized match: /api/user/{user-id} matches /api/user/1234
 * - If both exist for same method, exact match is returned
 * 
 * Performance notes:
 * - Endpoints are cached in-memory for read-heavy traffic
 * - Path matching is O(n) per request with optimized segment comparison
 * - Exact matches checked first for early termination
 * - Future optimization: Pre-compute and index paths for O(1) lookup
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MockApiServiceImpl implements MockApiService {

    private final CacheService cacheService;
    private final ResponseResolver responseResolver;

    @Override
    public MockApiResponse handleRequest(MockRequest request) {
        log.debug("Processing request: {}", request);

        try {
            // Step 1: Validate request has project context
            if (request.getProjectContext() == null) {
                log.warn("Request missing project context");
                return build404Response("Project context not provided");
            }

            UUID projectId = request.getProjectContext().getProjectId();

            // Extract context path and compute actual endpoint path
            String contextPath = request.getProjectContext().getContextPath();
            String actualPath = request.getPath();
            if (contextPath != null && !contextPath.isEmpty() && actualPath.startsWith(contextPath)) {
                actualPath = actualPath.substring(contextPath.length());
                if (actualPath.isEmpty()) {
                    actualPath = "/";
                }
            }

            log.debug("Project ID: {}, Method: {}, Context Path: {}, Actual Path: {}",
                    projectId, request.getMethod(), contextPath, actualPath);

            // Step 2: Load all endpoints for this project from cache
            List<Endpoint> endpoints = cacheService.getOrLoadEndpoints(projectId);
            log.debug("Loaded {} endpoints for project {}", endpoints.size(), projectId);

            // Step 3: Find matching endpoint by exact path + method match
            Optional<Endpoint> matchedEndpoint = findMatchingEndpoint(endpoints, request.getMethod(),
                    actualPath);

            if (matchedEndpoint.isEmpty()) {
                log.warn("No endpoint matched: method={}, path={}, projectId={}",
                        request.getMethod(), request.getPath(), projectId);
                return build404Response("Endpoint not found");
            }

            Endpoint endpoint = matchedEndpoint.get();
            log.debug("Matched endpoint: id={}, path={}, method={}", endpoint.getId(), endpoint.getPath(),
                    endpoint.getMethod());

            // Step 4: Use ResponseResolver to find best matching response
            try {
                ResponseEntity<?> responseEntity = responseResolver.resolve(request, endpoint.getId());

                // Step 5: Convert ResponseEntity to MockApiResponse
                MockApiResponse response = convertToMockApiResponse(responseEntity);
                log.info("Response resolved: status={}, contentType={}", response.getStatusCode(),
                        response.getContentType());
                return response;

            } catch (ResponseResolutionException e) {
                log.error("Failed to resolve response for endpoint: {}", endpoint.getId(), e);
                return build500Response("Failed to resolve response");
            }

        } catch (Exception e) {
            log.error("Unexpected error processing request: {}", request, e);
            return build500Response("Internal server error");
        }
    }

    /**
     * Find endpoint matching request path and method with smart path matching.
     * 
     * Matching precedence (highest to lowest):
     * 1. Exact path match + method match: /api/user == /api/user
     * 2. Parameterized path match + method match: /api/user/{user-id} ~=
     * /api/user/1234
     * 
     * Implementation:
     * - First pass: Search for exact matches (fast, predictable)
     * - Second pass: Search for parameterized matches (only if no exact match)
     * - Returns first best match found with precedence ordering
     *
     * @param endpoints List of endpoints to search
     * @param method    HTTP method (GET, POST, etc.)
     * @param path      Request path
     * @return Optional endpoint if found, preferring exact matches over
     *         parameterized
     */
    private Optional<Endpoint> findMatchingEndpoint(List<Endpoint> endpoints, String method, String path) {
        // First pass: Look for exact matches (highest precedence)
        Optional<Endpoint> exactMatch = endpoints.stream()
                .filter(e -> e.getMethod().equalsIgnoreCase(method) && e.getPath().equals(path))
                .findFirst();

        if (exactMatch.isPresent()) {
            log.debug("Found exact path match: {} {}", method, path);
            return exactMatch;
        }

        // Second pass: Look for parameterized matches (lower precedence)
        // Using PathMatcher to evaluate {parameter} patterns
        Optional<Endpoint> paramMatch = endpoints.stream()
                .filter(e -> e.getMethod().equalsIgnoreCase(method)
                        && PathMatcher.matches(e.getPath(), path))
                .findFirst();

        if (paramMatch.isPresent()) {
            log.debug("Found parameterized path match: {} {} matches {}", method, paramMatch.get().getPath(), path);
        }

        return paramMatch;
    }

    /**
     * Convert Spring ResponseEntity to MockApiResponse.
     * Extracts status code, headers, body, and content type.
     *
     * @param responseEntity Spring ResponseEntity from ResponseResolver
     * @return MockApiResponse
     */
    private MockApiResponse convertToMockApiResponse(ResponseEntity<?> responseEntity) {
        MockApiResponse.Builder builder = new MockApiResponse.Builder();

        // Set status code
        if (responseEntity.getStatusCode() != null) {
            builder.statusCode(responseEntity.getStatusCode().value());
        }

        // Set headers
        if (responseEntity.getHeaders() != null) {
            responseEntity.getHeaders().forEach((key, values) -> {
                if (!values.isEmpty()) {
                    builder.header(key, values.get(0));
                }
            });
        }

        // Set content type
        if (responseEntity.getHeaders() != null &&
                responseEntity.getHeaders().getContentType() != null) {
            builder.contentType(responseEntity.getHeaders().getContentType().toString());
        }

        // Set body
        if (responseEntity.getBody() != null) {
            builder.body(responseEntity.getBody().toString());
        }

        return builder.build();
    }

    /**
     * Build 404 Not Found response.
     *
     * @param message Error message
     * @return 404 MockApiResponse
     */
    private MockApiResponse build404Response(String message) {
        return new MockApiResponse.Builder()
                .statusCode(404)
                .contentType("application/json")
                .body("{\"error\": \"" + message + "\"}")
                .header("X-Error", "Not Found")
                .build();
    }

    /**
     * Build 500 Internal Server Error response.
     *
     * @param message Error message
     * @return 500 MockApiResponse
     */
    private MockApiResponse build500Response(String message) {
        return new MockApiResponse.Builder()
                .statusCode(500)
                .contentType("application/json")
                .body("{\"error\": \"" + message + "\"}")
                .header("X-Error", "Internal Server Error")
                .build();
    }
}

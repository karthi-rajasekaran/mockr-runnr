package com.mockr.runnr.web;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mockr.runnr.dto.MockApiResponse;
import com.mockr.runnr.dto.MockRequest;
import com.mockr.runnr.service.MockApiService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * MockApiController - Catch-all REST controller for mock request handling.
 * 
 * Accepts incoming HTTP requests on any path and method.
 * Captures all request details and delegates to service layer for processing.
 * Returns mock response from service layer.
 * 
 * Design:
 * - Thin controller focused on HTTP concerns only
 * - No business logic - all delegated to service
 * - Logs request/response for debugging
 * - All exceptions handled by global exception handler
 */
@Slf4j
@RestController
@RequestMapping("/**")
@RequiredArgsConstructor
public class MockApiController {

    private final MockApiService mockApiService;

    /**
     * Catch-all handler for all HTTP requests on any path and method.
     * 
     * Captures complete request details and passes to service for processing.
     */
    @RequestMapping
    public ResponseEntity<?> handleRequest(
            HttpServletRequest request,
            @RequestBody(required = false) String body) {

        try {
            // Log incoming request
            logRequest(request, body);

            // Extract project ID from header or generate default
            String projectId = extractProjectId(request);

            // Capture all request details
            MockRequest mockRequest = new MockRequest.Builder()
                    .method(request.getMethod())
                    .path(request.getRequestURI())
                    .body(body)
                    .headers(getAllHeaders(request))
                    .queryParameters(getAllQueryParameters(request))
                    .projectId(projectId)
                    .build();

            // Delegate to service for processing
            MockApiResponse response = mockApiService.handleRequest(mockRequest);

            // Build ResponseEntity with response details
            ResponseEntity<?> responseEntity = buildResponseEntity(response);

            // Log outgoing response
            logResponse(request, response);

            return responseEntity;

        } catch (Exception e) {
            // Log exception - will be handled by global exception handler
            log.error("Error processing request: {}", request.getRequestURI(), e);
            throw e;
        }
    }

    /**
     * Extract all HTTP headers from request.
     */
    private Map<String, String> getAllHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }

        return headers;
    }

    /**
     * Extract all query parameters from request.
     */
    private Map<String, String> getAllQueryParameters(HttpServletRequest request) {
        Map<String, String> queryParams = new HashMap<>();

        if (request.getQueryString() != null && !request.getQueryString().isEmpty()) {
            Map<String, String[]> parameterMap = request.getParameterMap();
            parameterMap.forEach((key, values) -> {
                if (values.length > 0) {
                    queryParams.put(key, values[0]); // Take first value if multiple
                }
            });
        }

        return queryParams;
    }

    /**
     * Extract project ID from request.
     * Looks for X-Project-Id header, falls back to default.
     */
    private String extractProjectId(HttpServletRequest request) {
        String projectId = request.getHeader("X-Project-Id");
        if (projectId == null || projectId.isBlank()) {
            // Default project ID if not provided
            projectId = UUID.randomUUID().toString();
            log.debug("No X-Project-Id header found, using generated ID: {}", projectId);
        }
        return projectId;
    }

    /**
     * Build ResponseEntity from MockApiResponse.
     */
    private ResponseEntity<?> buildResponseEntity(MockApiResponse response) {
        HttpHeaders headers = new HttpHeaders();

        // Add all response headers
        response.getHeaders().forEach(headers::set);

        // Add content type if present
        if (response.getContentType() != null && !response.getContentType().isBlank()) {
            headers.set(HttpHeaders.CONTENT_TYPE, response.getContentType());
        }

        // Build response entity with status code, body, and headers
        return new ResponseEntity<>(
                response.getBody(),
                headers,
                HttpStatus.valueOf(response.getStatusCode()));
    }

    /**
     * Log incoming request details.
     */
    private void logRequest(HttpServletRequest request, String body) {
        log.info("Incoming request: {} {} | Headers: {} | Body size: {} bytes",
                request.getMethod(),
                request.getRequestURI(),
                getAllHeaders(request).size(),
                body != null ? body.length() : 0);
    }

    /**
     * Log outgoing response details.
     */
    private void logResponse(HttpServletRequest request, MockApiResponse response) {
        log.info("Outgoing response: {} {} -> Status: {} | Headers: {} | Body size: {} bytes",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatusCode(),
                response.getHeaders().size(),
                response.getBody() != null ? response.getBody().length() : 0);
    }
}

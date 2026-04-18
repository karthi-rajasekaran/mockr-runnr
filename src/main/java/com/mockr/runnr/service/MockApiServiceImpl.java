package com.mockr.runnr.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mockr.runnr.dto.MockApiResponse;
import com.mockr.runnr.dto.MockRequest;

/**
 * MockApiServiceImpl - Default implementation of MockApiService.
 * 
 * Placeholder for now - full implementation will come in later issues:
 * - Issue #6: Path Matcher for endpoint resolution
 * - Issue #7: Condition Evaluator for rule matching
 * - Issue #8: Response Builder for response construction
 * - Issue #9: Fallback Handler for default responses
 * 
 * For now, returns a basic 200 OK response for testing.
 */
@Service
public class MockApiServiceImpl implements MockApiService {

    private static final Logger logger = LoggerFactory.getLogger(MockApiServiceImpl.class);

    @Override
    public MockApiResponse handleRequest(MockRequest request) {
        logger.debug("Processing request: {}", request);

        // Placeholder response - will be replaced with actual logic
        return new MockApiResponse.Builder()
                .statusCode(200)
                .contentType("application/json")
                .body("{\"message\": \"Mock API Response - Service implementation pending\"}")
                .header("X-Mock-API", "Mockr-Runnr")
                .build();
    }
}

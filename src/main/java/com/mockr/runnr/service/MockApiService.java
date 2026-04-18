package com.mockr.runnr.service;

import com.mockr.runnr.dto.MockApiResponse;
import com.mockr.runnr.dto.MockRequest;

/**
 * MockApiService - Service layer contract for mock request processing.
 * 
 * Handles the business logic of:
 * - Project resolution
 * - Endpoint matching
 * - Condition evaluation
 * - Response building
 * 
 * Controller delegates all request processing to this service.
 */
public interface MockApiService {

    /**
     * Process incoming mock request and return mock response.
     * 
     * @param request MockRequest with all captured request details
     * @return MockApiResponse with the mock response to return
     */
    MockApiResponse handleRequest(MockRequest request);
}

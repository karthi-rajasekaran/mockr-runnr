package com.mockr.runnr.service.resolver;

import com.mockr.runnr.dto.MockRequest;
import com.mockr.runnr.domain.Response;
import com.mockr.runnr.domain.ResponseHeader;
import com.mockr.runnr.exception.ResponseResolutionException;
import com.mockr.runnr.matcher.ConditionEvaluator;
import com.mockr.runnr.matcher.EvaluationContext;
import com.mockr.runnr.builder.ResponseBuilder;
import com.mockr.runnr.builder.ResponseStrategyFactory;
import com.mockr.runnr.builder.ResponseType;
import com.mockr.runnr.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Smart response resolver service.
 * Main orchestrator for evaluating all responses for an endpoint
 * and selecting the best match based on condition specificity and tie-breaking
 * rules.
 * 
 * This service:
 * 1. Loads endpoint and its responses from cache (Issue 5)
 * 2. Evaluates conditions using ConditionEvaluator (Issue 7)
 * 3. Scores responses by condition count (specificity)
 * 4. Applies deterministic tie-breaking (createdAt, responseId)
 * 5. Constructs response using ResponseBuilder (Issue 8)
 * 6. Returns 404 if no responses match
 */
@Service
@RequiredArgsConstructor
public class ResponseResolver {
    private static final Logger logger = LoggerFactory.getLogger(ResponseResolver.class);

    // Dependencies from Issues 5, 7, 8
    private final ConditionEvaluator conditionEvaluator;
    private final ResponseStrategyFactory responseStrategyFactory;
    private final CacheService cacheService;
    private final ResponseSelectionEngine selectionEngine;

    /**
     * Main entry point: Resolve incoming request to best matching response.
     *
     * Flow:
     * 1. Load endpoint from cache (Issue 5) - returns Endpoint with nested Response
     * + Condition objects
     * 2. Get responses from endpoint.getResponses()
     * 3. Evaluate each response sequentially
     * 4. Score = number of matched conditions
     * 5. Select best using tie-breaking: score DESC, createdAt DESC, responseId
     * DESC
     * 6. Construct response using ResponseBuilder
     * 7. Return 404 if no matches
     *
     * @param mockRequest Incoming HTTP request with headers, query params, body
     * @param endpointId  Already-matched endpoint UUID from PathMatcher (Issue 6)
     * @return ResponseEntity with best matching response or 404
     * @throws ResponseResolutionException if resolution fails
     */
    public ResponseEntity<?> resolve(MockRequest mockRequest, java.util.UUID endpointId) {
        logger.info("Resolving response for endpoint {} with request method={} path={}",
                endpointId, mockRequest.getMethod(), mockRequest.getPath());

        try {
            // Step 1: Load endpoint from cache (Issue 5) - includes nested responses and
            // conditions
            Optional<com.mockr.runnr.domain.Endpoint> endpointOpt = cacheService.getOrLoadEndpoint(endpointId);

            if (endpointOpt.isEmpty()) {
                logger.warn("Endpoint not found: {}", endpointId);
                return buildNotFoundResponse();
            }

            com.mockr.runnr.domain.Endpoint endpoint = endpointOpt.get();
            List<Response> responses = new ArrayList<>(endpoint.getResponses());
            logger.debug("Loaded {} responses for endpoint {}", responses.size(), endpointId);

            // Step 2: Evaluate each response sequentially (no parallelism for MVP)
            List<ResponseCandidate> candidates = new ArrayList<>();

            for (Response response : responses) {
                // Get conditions for this response (already loaded with endpoint)
                List<com.mockr.runnr.domain.Condition> conditions = new ArrayList<>(response.getConditions());

                logger.debug("Evaluating response id={} with {} conditions",
                        response.getId(), conditions.size());

                // Use ConditionEvaluator (Issue 7) to check ALL conditions match
                boolean allMatch = conditionEvaluator.evaluate(
                        conditions,
                        toEvaluationContext(mockRequest));

                if (allMatch) {
                    // Score = number of matched conditions (specificity)
                    int score = conditions.size();

                    // Convert ResponseHeaders Set to Map
                    Map<String, String> headers = new HashMap<>();
                    if (response.getResponseHeaders() != null) {
                        for (ResponseHeader header : response.getResponseHeaders()) {
                            headers.put(header.getHeaderKey(), header.getHeaderValue());
                        }
                    }

                    ResponseCandidate candidate = new ResponseCandidate(
                            response.getId(),
                            response.getStatusCode(),
                            headers,
                            response.getResponseBody(),
                            response.getContentType(),
                            response.getCreatedAt(),
                            score);

                    candidates.add(candidate);
                    logger.debug("Response id={} matched with score={}", response.getId(), score);
                } else {
                    logger.debug("Response id={} rejected - not all conditions matched",
                            response.getId());
                }
            }

            // Step 3: Select best response or return 404
            if (candidates.isEmpty()) {
                logger.warn("No matching responses for endpoint {}", endpointId);
                return buildNotFoundResponse();
            }

            Optional<ResponseCandidate> bestCandidate = selectionEngine.findBestMatch(candidates);

            if (bestCandidate.isEmpty()) {
                logger.warn("No candidate selected for endpoint {}", endpointId);
                return buildNotFoundResponse();
            }

            ResponseCandidate best = bestCandidate.get();

            // Step 4: Construct response using ResponseBuilder with headers and content type
            ResponseEntity<?> response = buildResponseEntity(best);

            logger.info("Response resolved: id={}, score={}, contentType={}",
                    best.responseId(), best.score(), best.responseType());

            return response;

        } catch (Exception e) {
            logger.error("Error resolving response for endpoint {}", endpointId, e);
            throw new ResponseResolutionException("Failed to resolve response for endpoint " + endpointId, e);
        }
    }

    /**
     * Build HTTP 404 Not Found response.
     * 
     * @return 404 ResponseEntity
     */
    private ResponseEntity<?> buildNotFoundResponse() {
        return ResponseEntity.notFound().build();
    }

    /**
     * Convert MockRequest to EvaluationContext for ConditionEvaluator (Issue 7).
     * EvaluationContext holds headers, query params, and path variables needed for
     * condition evaluation.
     *
     * @param mockRequest Incoming request to convert
     * @return EvaluationContext for condition evaluation
     */
    private EvaluationContext toEvaluationContext(MockRequest mockRequest) {
        // Note: Adjust based on actual EvaluationContext builder structure
        Map<String, String> pathVariables = new HashMap<>(); // Extract from request if available

        return EvaluationContext.builder()
                .headers(mockRequest.getHeaders())
                .queryParameters(mockRequest.getQueryParameters())
                .pathVariables(pathVariables)
                .build();
    }

    /**
     * Build ResponseEntity using ResponseBuilder with proper headers and content type handling.
     * This method:
     * 1. Creates a new ResponseBuilder instance
     * 2. Sets status code, body, and content type
     * 3. Adds custom headers from the response candidate
     * 4. Delegates to strategy factory for content-type-specific handling
     *
     * @param candidate Best matching response candidate
     * @return ResponseEntity with proper headers, status, and body
     */
    private ResponseEntity<?> buildResponseEntity(ResponseCandidate candidate) {
        ResponseType responseType = mapContentTypeToResponseType(candidate.responseType());

        ResponseBuilder builder = new ResponseBuilder()
                .withStatus(candidate.statusCode())
                .withBody(candidate.body() != null ? candidate.body() : "")
                .withResponseType(responseType);

        // Add custom headers from the response
        if (candidate.headers() != null && !candidate.headers().isEmpty()) {
            for (Map.Entry<String, String> entry : candidate.headers().entrySet()) {
                builder.withHeader(entry.getKey(), entry.getValue());
            }
        }

        logger.debug("Built response with status={}, type={}, headers={}",
                candidate.statusCode(), responseType, candidate.headers().size());

        return builder.build(responseStrategyFactory);
    }

    /**
     * Map content type string to ResponseType enum.
     * This is used to select the appropriate response strategy.
     *
     * @param contentTypeString Content type from response (e.g., "application/json")
     * @return ResponseType enum matching the content type, defaults to JSON if unknown
     */
    private ResponseType mapContentTypeToResponseType(String contentTypeString) {
        if (contentTypeString == null || contentTypeString.isBlank()) {
            return ResponseType.JSON;
        }

        return switch (contentTypeString.toLowerCase()) {
            case "application/json" -> ResponseType.JSON;
            case "application/xml" -> ResponseType.XML;
            case "text/html" -> ResponseType.HTML;
            case "application/octet-stream", "application/pdf" -> ResponseType.FILE;
            case "application/soap+xml" -> ResponseType.SOAP;
            case "application/graphql+json" -> ResponseType.GRAPHQL;
            case "binary", "application/binary" -> ResponseType.BINARY;
            default -> {
                logger.warn("Unknown content type: {}, defaulting to JSON", contentTypeString);
                yield ResponseType.JSON;
            }
        };
    }
}

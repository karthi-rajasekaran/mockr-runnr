package com.mockr.runnr.service.resolver;

import com.mockr.runnr.dto.MockRequest;
import com.mockr.runnr.domain.Condition;
import com.mockr.runnr.domain.Endpoint;
import com.mockr.runnr.domain.Response;
import com.mockr.runnr.domain.ConditionOperator;
import com.mockr.runnr.exception.ResponseResolutionException;
import com.mockr.runnr.matcher.ConditionEvaluator;
import com.mockr.runnr.matcher.EvaluationContext;
import com.mockr.runnr.service.CacheService;
import com.mockr.runnr.builder.ResponseStrategyFactory;
import com.mockr.runnr.builder.ResponseStrategy;
import com.mockr.runnr.builder.ResponseType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ResponseResolver service.
 * Tests response selection logic, tie-breaking, and 404 handling.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ResponseResolver Tests")
class ResponseResolverTest {

        @Mock
        private ConditionEvaluator conditionEvaluator;

        @Mock
        private ResponseStrategyFactory responseStrategyFactory;

        @Mock
        private CacheService cacheService;

        @Mock
        private ResponseSelectionEngine selectionEngine;

        @Mock
        private ResponseStrategy mockStrategy;

        @InjectMocks
        private ResponseResolver responseResolver;

        private UUID endpointId;
        private MockRequest mockRequest;

        @BeforeEach
        void setup() {
                endpointId = UUID.randomUUID();
                mockRequest = new MockRequest.Builder()
                                .method("POST")
                                .path("/api/payment")
                                .headers(Map.of("x-api-key", "123", "x-corr-id", "abc123"))
                                .queryParameters(Map.of())
                                .build();
        }

        private void setupResponseStrategyMocks() {
                when(responseStrategyFactory.getStrategy(any(ResponseType.class)))
                                .thenReturn(mockStrategy);
                when(mockStrategy.handle(any()))
                                .thenReturn(ResponseEntity.ok().build());
        }

        @Test
        @DisplayName("Should return best matching response when single response matches")
        void testResolve_SingleResponseMatches() {
                setupResponseStrategyMocks();

                UUID responseId = UUID.randomUUID();
                Response response = Response.builder()
                                .id(responseId)
                                .statusCode(200)
                                .contentType("application/json")
                                .responseBody("{\"status\": \"ok\"}")
                                .createdAt(LocalDateTime.now())
                                .conditions(new HashSet<>(List.of(
                                                Condition.builder()
                                                                .lhs("header.x-api-key")
                                                                .operation(ConditionOperator.EQ)
                                                                .rhs("123")
                                                                .build())))
                                .build();

                Endpoint endpoint = Endpoint.builder()
                                .id(endpointId)
                                .responses(new HashSet<>(List.of(response)))
                                .build();

                when(cacheService.getOrLoadEndpoint(endpointId)).thenReturn(Optional.of(endpoint));
                when(conditionEvaluator.evaluate(anyList(), any(EvaluationContext.class)))
                                .thenReturn(true);

                ResponseCandidate candidate = new ResponseCandidate(
                                responseId, 200, Map.of("Content-Type", "application/json"),
                                "{\"status\": \"ok\"}", "application/json", LocalDateTime.now(), 1);
                when(selectionEngine.findBestMatch(anyList())).thenReturn(Optional.of(candidate));

                ResponseEntity<?> result = responseResolver.resolve(mockRequest, endpointId);

                assertNotNull(result);
                assertEquals(HttpStatus.OK, result.getStatusCode());
        }

        @Test
        @DisplayName("Should return 404 when no responses match conditions")
        void testResolve_NoResponseMatches_Returns404() {
                UUID responseId = UUID.randomUUID();
                Response response = Response.builder()
                                .id(responseId)
                                .statusCode(200)
                                .contentType("application/json")
                                .createdAt(LocalDateTime.now())
                                .conditions(new HashSet<>(List.of(
                                                Condition.builder()
                                                                .lhs("header.x-api-key")
                                                                .operation(ConditionOperator.EQ)
                                                                .rhs("invalid")
                                                                .build())))
                                .build();

                Endpoint endpoint = Endpoint.builder()
                                .id(endpointId)
                                .responses(new HashSet<>(List.of(response)))
                                .build();

                when(cacheService.getOrLoadEndpoint(endpointId)).thenReturn(Optional.of(endpoint));
                when(conditionEvaluator.evaluate(anyList(), any(EvaluationContext.class)))
                                .thenReturn(false);

                ResponseEntity<?> result = responseResolver.resolve(mockRequest, endpointId);

                assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        }

        @Test
        @DisplayName("Should return 404 when endpoint not found")
        void testResolve_EndpointNotFound_Returns404() {
                when(cacheService.getOrLoadEndpoint(endpointId)).thenReturn(Optional.empty());

                ResponseEntity<?> result = responseResolver.resolve(mockRequest, endpointId);

                assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        }

        @Test
        @DisplayName("Should throw ResponseResolutionException on cache error")
        void testResolve_CacheError_ThrowsException() {
                when(cacheService.getOrLoadEndpoint(endpointId))
                                .thenThrow(new RuntimeException("Cache error"));

                assertThrows(ResponseResolutionException.class,
                                () -> responseResolver.resolve(mockRequest, endpointId));
        }
}

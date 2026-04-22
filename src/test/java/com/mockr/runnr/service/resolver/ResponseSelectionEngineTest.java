package com.mockr.runnr.service.resolver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ResponseSelectionEngine.
 * Tests tie-breaking logic and candidate selection.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ResponseSelectionEngine Tests")
class ResponseSelectionEngineTest {

        @InjectMocks
        private ResponseSelectionEngine selectionEngine;

        private static final Map<String, String> EMPTY_HEADERS = Map.of();

        @Test
        @DisplayName("Should select response with highest score")
        void testFindBestMatch_HighestScore() {
                // Setup: 3 responses, different scores
                UUID id1 = UUID.randomUUID();
                UUID id2 = UUID.randomUUID();
                UUID id3 = UUID.randomUUID();

                ResponseCandidate candidate1 = new ResponseCandidate(id1, 200, EMPTY_HEADERS, "body1",
                                "application/json", LocalDateTime.now(), 1);
                ResponseCandidate candidate2 = new ResponseCandidate(id2, 200, EMPTY_HEADERS, "body2",
                                "application/json", LocalDateTime.now(), 3); // Higher score
                ResponseCandidate candidate3 = new ResponseCandidate(id3, 200, EMPTY_HEADERS, "body3",
                                "application/json", LocalDateTime.now(), 2);

                // Execute
                Optional<ResponseCandidate> result = selectionEngine
                                .findBestMatch(List.of(candidate1, candidate2, candidate3));

                // Assert
                assertTrue(result.isPresent());
                assertEquals(id2, result.get().responseId());
                assertEquals(3, result.get().score());
        }

        @Test
        @DisplayName("Should apply createdAt tie-breaker when scores equal")
        void testFindBestMatch_TieBreaker_CreatedAt() {
                // Setup: Same score, different createdAt (latest = highest priority)
                LocalDateTime older = LocalDateTime.of(2026, 1, 1, 10, 0);
                LocalDateTime newer = LocalDateTime.of(2026, 1, 2, 10, 0);

                // Use fixed UUIDs for deterministic testing
                UUID id1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
                UUID id2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
                UUID id3 = UUID.fromString("00000000-0000-0000-0000-000000000003");

                ResponseCandidate candidate1 = new ResponseCandidate(id1, 200, EMPTY_HEADERS, "body1",
                                "application/json", older, 2);
                ResponseCandidate candidate2 = new ResponseCandidate(id2, 200, EMPTY_HEADERS, "body2",
                                "application/json", newer, 2); // Newer
                ResponseCandidate candidate3 = new ResponseCandidate(id3, 200, EMPTY_HEADERS, "body3",
                                "application/json", older, 2);

                // Execute
                Optional<ResponseCandidate> result = selectionEngine
                                .findBestMatch(List.of(candidate1, candidate2, candidate3));

                // Assert
                assertTrue(result.isPresent());
                assertEquals(id2, result.get().responseId());
                assertEquals(newer, result.get().createdAt());
        }

        @Test
        @DisplayName("Should apply responseId tie-breaker when all else equal (deterministic)")
        void testFindBestMatch_TieBreaker_ResponseId() {
                // Setup: Same score and createdAt, different IDs (highest UUID wins)
                LocalDateTime sameTime = LocalDateTime.of(2026, 1, 1, 10, 0);

                UUID id1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
                UUID id2 = UUID.fromString("00000000-0000-0000-0000-000000000005"); // Highest
                UUID id3 = UUID.fromString("00000000-0000-0000-0000-000000000002");

                ResponseCandidate candidate1 = new ResponseCandidate(id1, 200, EMPTY_HEADERS, "body1",
                                "application/json", sameTime, 2);
                ResponseCandidate candidate2 = new ResponseCandidate(id2, 200, EMPTY_HEADERS, "body2",
                                "application/json", sameTime, 2); // Highest UUID
                ResponseCandidate candidate3 = new ResponseCandidate(id3, 200, EMPTY_HEADERS, "body3",
                                "application/json", sameTime, 2);

                // Execute
                Optional<ResponseCandidate> result = selectionEngine
                                .findBestMatch(List.of(candidate1, candidate2, candidate3));

                // Assert
                assertTrue(result.isPresent());
                assertEquals(id2, result.get().responseId());
        }

        @Test
        @DisplayName("Should handle empty candidate list")
        void testFindBestMatch_EmptyList() {
                // Execute
                Optional<ResponseCandidate> result = selectionEngine.findBestMatch(List.of());

                // Assert
                assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should handle null candidate list")
        void testFindBestMatch_NullList() {
                // Execute
                Optional<ResponseCandidate> result = selectionEngine.findBestMatch(null);

                // Assert
                assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should handle single candidate")
        void testFindBestMatch_SingleCandidate() {
                // Setup
                UUID id = UUID.randomUUID();
                ResponseCandidate candidate = new ResponseCandidate(id, 200, EMPTY_HEADERS, "body",
                                "application/json", LocalDateTime.now(), 1);

                // Execute
                Optional<ResponseCandidate> result = selectionEngine.findBestMatch(List.of(candidate));

                // Assert
                assertTrue(result.isPresent());
                assertEquals(id, result.get().responseId());
        }

        @Test
        @DisplayName("Should calculate score correctly")
        void testCalculateScore() {
                // Test various condition counts
                assertEquals(0, selectionEngine.calculateScore(0));
                assertEquals(1, selectionEngine.calculateScore(1));
                assertEquals(5, selectionEngine.calculateScore(5));
                assertEquals(10, selectionEngine.calculateScore(10));
        }

        @Test
        @DisplayName("Full tie-breaking scenario with real-world data")
        void testFindBestMatch_ComplexScenario() {
                // Real-world scenario: POST /payment endpoint
                // Multiple responses with various scores and timestamps
                LocalDateTime baseTime = LocalDateTime.of(2026, 1, 1, 12, 0);

                UUID id1 = UUID.randomUUID();
                UUID id2 = UUID.randomUUID();
                UUID id3 = UUID.randomUUID();
                UUID id4 = UUID.randomUUID();

                ResponseCandidate response1 = new ResponseCandidate(
                                id1, 200, EMPTY_HEADERS, "body1", "application/json", baseTime, 1);

                ResponseCandidate response2 = new ResponseCandidate(
                                id2, 200, EMPTY_HEADERS, "body2", "application/json", baseTime.plusHours(1), 2);

                ResponseCandidate response3 = new ResponseCandidate(
                                id3, 200, EMPTY_HEADERS, "body3", "application/json", baseTime.minusHours(1), 2);

                ResponseCandidate response4 = new ResponseCandidate(
                                id4, 200, EMPTY_HEADERS, "body4", "application/json", baseTime.plusHours(1), 3); // Highest
                                                                                                                 // score

                // Execute
                Optional<ResponseCandidate> result = selectionEngine.findBestMatch(
                                List.of(response1, response2, response3, response4));

                // Assert: response4 should win (score 3 > 2)
                assertTrue(result.isPresent());
                assertEquals(id4, result.get().responseId());
                assertEquals(3, result.get().score());
        }
}

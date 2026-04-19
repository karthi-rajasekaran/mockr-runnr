package com.mockr.runnr.builder;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MockResponseTest {

    @Test
    void shouldCreateMockResponseWithBuilder() {
        MockResponse response = MockResponse.builder()
                .statusCode(200)
                .body("{\"message\": \"hello\"}")
                .contentType("application/json")
                .header("X-Custom", "value")
                .build();

        assertEquals(200, response.statusCode());
        assertEquals("{\"message\": \"hello\"}", response.body());
        assertEquals("application/json", response.contentType());
        assertEquals("value", response.headers().get("X-Custom"));
    }

    @Test
    void shouldDefaultStatusTo200() {
        MockResponse response = MockResponse.builder()
                .body("test")
                .build();

        assertEquals(200, response.statusCode());
    }

    @Test
    void shouldDefaultContentTypeToJson() {
        MockResponse response = MockResponse.builder()
                .build();

        assertEquals("application/json", response.contentType());
    }

    @Test
    void shouldReturnImmutableHeaders() {
        MockResponse response = MockResponse.builder()
                .header("X-Key", "value")
                .build();

        assertThrows(UnsupportedOperationException.class,
                () -> response.headers().put("New-Key", "new-value"));
    }

    @Test
    void shouldHandleNullHeaders() {
        MockResponse response = new MockResponse(200, "test", "application/json", null);
        assertTrue(response.headers().isEmpty());
    }

    @Test
    void shouldAllowMultipleHeaders() {
        MockResponse response = MockResponse.builder()
                .header("X-Header-1", "value1")
                .header("X-Header-2", "value2")
                .header("X-Header-3", "value3")
                .build();

        assertEquals(3, response.headers().size());
        assertEquals("value1", response.headers().get("X-Header-1"));
        assertEquals("value2", response.headers().get("X-Header-2"));
        assertEquals("value3", response.headers().get("X-Header-3"));
    }

    @Test
    void shouldAddHeadersViaMap() {
        Map<String, String> headers = Map.of(
                "Content-Type", "application/json",
                "Authorization", "Bearer token");
        MockResponse response = MockResponse.builder()
                .headers(headers)
                .build();

        assertEquals(2, response.headers().size());
        assertEquals("application/json", response.headers().get("Content-Type"));
        assertEquals("Bearer token", response.headers().get("Authorization"));
    }
}

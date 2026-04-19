package com.mockr.runnr.matcher;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EvaluationContextTest {

    @Test
    void shouldResolveHeaderCaseInsensitive() {
        EvaluationContext ctx = EvaluationContext.builder()
                .headers(Map.of("X-API-Key", "secret"))
                .queryParameters(Map.of())
                .pathVariables(Map.of())
                .build();

        assertEquals("secret", ctx.resolve("header.x-api-key"));
        assertEquals("secret", ctx.resolve("header.X-API-Key"));
    }

    @Test
    void shouldResolveQueryParam() {
        EvaluationContext ctx = EvaluationContext.builder()
                .headers(Map.of())
                .queryParameters(Map.of("limit", "25"))
                .pathVariables(Map.of())
                .build();

        assertEquals("25", ctx.resolve("query.limit"));
    }

    @Test
    void shouldResolvePathVariable() {
        EvaluationContext ctx = EvaluationContext.builder()
                .headers(Map.of())
                .queryParameters(Map.of())
                .pathVariables(Map.of("id", "42"))
                .build();

        assertEquals("42", ctx.resolve("path.id"));
    }

    @Test
    void shouldReturnNullForUnknownSource() {
        EvaluationContext ctx = EvaluationContext.builder()
                .headers(Map.of())
                .queryParameters(Map.of())
                .pathVariables(Map.of())
                .build();

        assertNull(ctx.resolve("body.field"));
    }

    @Test
    void shouldReturnNullForNullKey() {
        EvaluationContext ctx = EvaluationContext.builder()
                .headers(Map.of())
                .queryParameters(Map.of())
                .pathVariables(Map.of())
                .build();

        assertNull(ctx.resolve(null));
    }

    @Test
    void shouldReturnNullForKeyWithoutDot() {
        EvaluationContext ctx = EvaluationContext.builder()
                .headers(Map.of())
                .queryParameters(Map.of())
                .pathVariables(Map.of())
                .build();

        assertNull(ctx.resolve("noDotKey"));
    }

    @Test
    void shouldReturnImmutableMaps() {
        EvaluationContext ctx = EvaluationContext.builder()
                .headers(Map.of("h", "v"))
                .queryParameters(Map.of("q", "v"))
                .pathVariables(Map.of("p", "v"))
                .build();

        assertThrows(UnsupportedOperationException.class, () -> ctx.getHeaders().put("x", "y"));
        assertThrows(UnsupportedOperationException.class, () -> ctx.getQueryParameters().put("x", "y"));
        assertThrows(UnsupportedOperationException.class, () -> ctx.getPathVariables().put("x", "y"));
    }
}

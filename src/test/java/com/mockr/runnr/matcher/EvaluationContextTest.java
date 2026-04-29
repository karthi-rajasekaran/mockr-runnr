package com.mockr.runnr.matcher;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EvaluationContext - Unified Field Map")
class EvaluationContextTest {

    @Test
    @DisplayName("Resolve header field with full path key")
    void shouldResolveHeaderField() {
        EvaluationContext ctx = EvaluationContext.builder()
                .addField("header.x-api-key", "secret")
                .addField("header.content-type", "application/json")
                .build();

        assertEquals("secret", ctx.resolveField("header.x-api-key"));
        assertEquals("application/json", ctx.resolveField("header.content-type"));
    }

    @Test
    @DisplayName("Resolve query parameter field")
    void shouldResolveQueryField() {
        EvaluationContext ctx = EvaluationContext.builder()
                .addField("query.limit", "25")
                .addField("query.page", "1")
                .build();

        assertEquals("25", ctx.resolveField("query.limit"));
        assertEquals("1", ctx.resolveField("query.page"));
    }

    @Test
    @DisplayName("Resolve path variable field")
    void shouldResolvePathField() {
        EvaluationContext ctx = EvaluationContext.builder()
                .addField("path.userId", "42")
                .build();

        assertEquals("42", ctx.resolveField("path.userId"));
    }

    @Test
    @DisplayName("Resolve body field from flattened JSON")
    void shouldResolveBodyField() {
        EvaluationContext ctx = EvaluationContext.builder()
                .addField("body.username", "karthi")
                .addField("body.password", "Welcome@01")
                .addField("body.items[0].id", 1)
                .build();

        assertEquals("karthi", ctx.resolveField("body.username"));
        assertEquals("Welcome@01", ctx.resolveField("body.password"));
        assertEquals(1, ctx.resolveField("body.items[0].id"));
    }

    @Test
    @DisplayName("Resolve with nested objects")
    void shouldResolveNestedObjectFields() {
        EvaluationContext ctx = EvaluationContext.builder()
                .addField("body.user.name", "John")
                .addField("body.user.email", "john@example.com")
                .build();

        assertEquals("John", ctx.resolveField("body.user.name"));
        assertEquals("john@example.com", ctx.resolveField("body.user.email"));
    }

    @Test
    @DisplayName("Return null for non-existent field")
    void shouldReturnNullForMissingField() {
        EvaluationContext ctx = EvaluationContext.builder()
                .addField("header.content-type", "application/json")
                .build();

        assertNull(ctx.resolveField("body.nonexistent"));
        assertNull(ctx.resolveField("query.missing"));
    }

    @Test
    @DisplayName("Return null for null or empty key")
    void shouldReturnNullForNullKey() {
        EvaluationContext ctx = EvaluationContext.builder()
                .addField("header.test", "value")
                .build();

        assertNull(ctx.resolveField(null));
        assertNull(ctx.resolveField(""));
    }

    @Test
    @DisplayName("Build context from fields map")
    void shouldBuildFromFieldsMap() {
        Map<String, Object> fields = Map.of(
                "header.authorization", "Bearer token",
                "query.page", "1",
                "body.username", "admin");

        EvaluationContext ctx = EvaluationContext.builder()
                .fields(fields)
                .build();

        assertEquals("Bearer token", ctx.resolveField("header.authorization"));
        assertEquals("1", ctx.resolveField("query.page"));
        assertEquals("admin", ctx.resolveField("body.username"));
    }

    @Test
    @DisplayName("Fields map is immutable")
    void shouldReturnImmutableFieldsMap() {
        EvaluationContext ctx = EvaluationContext.builder()
                .addField("header.test", "value")
                .build();

        assertThrows(UnsupportedOperationException.class,
                () -> ctx.getFields().put("header.new", "newvalue"));
    }

    @Test
    @DisplayName("Support numeric and boolean values")
    void shouldSupportMultipleTypes() {
        EvaluationContext ctx = EvaluationContext.builder()
                .addField("body.count", 42)
                .addField("body.price", 99.99)
                .addField("body.active", true)
                .addField("body.email", null)
                .build();

        assertEquals(42, ctx.resolveField("body.count"));
        assertEquals(99.99, ctx.resolveField("body.price"));
        assertEquals(true, ctx.resolveField("body.active"));
        assertNull(ctx.resolveField("body.email"));
    }
}

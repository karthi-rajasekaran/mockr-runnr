package com.mockr.runnr.matcher.body;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JsonBodyParser - JSON Flattening with Arrays")
class JsonBodyParserTest {

    private final JsonBodyParser parser = new JsonBodyParser();

    @Test
    @DisplayName("Simple object flattening with dot notation")
    void testSimpleObjectFlattening() throws BodyParser.BodyParseException {
        String json = """
                {
                  "user": {
                    "name": "John",
                    "email": "john@example.com"
                  },
                  "role": "admin"
                }
                """;

        Map<String, Object> result = parser.parse(json);

        assertEquals("John", result.get("user.name"));
        assertEquals("john@example.com", result.get("user.email"));
        assertEquals("admin", result.get("role"));
    }

    @Test
    @DisplayName("Array of objects with bracket indexing")
    void testArrayOfObjects() throws BodyParser.BodyParseException {
        String json = """
                {
                  "items": [
                    {"id": 1, "name": "Product A"},
                    {"id": 2, "name": "Product B"}
                  ]
                }
                """;

        Map<String, Object> result = parser.parse(json);

        assertEquals(1, result.get("items[0].id"));
        assertEquals("Product A", result.get("items[0].name"));
        assertEquals(2, result.get("items[1].id"));
        assertEquals("Product B", result.get("items[1].name"));
    }

    @Test
    @DisplayName("Array of primitives")
    void testArrayOfPrimitives() throws BodyParser.BodyParseException {
        String json = """
                {
                  "tags": ["electronics", "gadgets", "premium"],
                  "scores": [95, 87, 92]
                }
                """;

        Map<String, Object> result = parser.parse(json);

        assertEquals("electronics", result.get("tags[0]"));
        assertEquals("gadgets", result.get("tags[1]"));
        assertEquals("premium", result.get("tags[2]"));
        assertEquals(95, result.get("scores[0]"));
        assertEquals(87, result.get("scores[1]"));
        assertEquals(92, result.get("scores[2]"));
    }

    @Test
    @DisplayName("Nested arrays (multi-level)")
    void testNestedArrays() throws BodyParser.BodyParseException {
        String json = """
                {
                  "matrix": [
                    [1, 2, 3],
                    [4, 5, 6]
                  ]
                }
                """;

        Map<String, Object> result = parser.parse(json);

        assertEquals(1, result.get("matrix[0][0]"));
        assertEquals(2, result.get("matrix[0][1]"));
        assertEquals(3, result.get("matrix[0][2]"));
        assertEquals(4, result.get("matrix[1][0]"));
        assertEquals(5, result.get("matrix[1][1]"));
        assertEquals(6, result.get("matrix[1][2]"));
    }

    @Test
    @DisplayName("Complex mixed structure with nested objects and arrays")
    void testComplexMixedStructure() throws BodyParser.BodyParseException {
        String json = """
                {
                  "users": [
                    {
                      "id": 1,
                      "name": "John",
                      "addresses": [
                        {"city": "NYC"},
                        {"city": "LA"}
                      ]
                    },
                    {
                      "id": 2,
                      "name": "Jane",
                      "addresses": [{"city": "Chicago"}]
                    }
                  ]
                }
                """;

        Map<String, Object> result = parser.parse(json);

        // First user
        assertEquals(1, result.get("users[0].id"));
        assertEquals("John", result.get("users[0].name"));
        assertEquals("NYC", result.get("users[0].addresses[0].city"));
        assertEquals("LA", result.get("users[0].addresses[1].city"));

        // Second user
        assertEquals(2, result.get("users[1].id"));
        assertEquals("Jane", result.get("users[1].name"));
        assertEquals("Chicago", result.get("users[1].addresses[0].city"));
    }

    @Test
    @DisplayName("Handles null values")
    void testNullValues() throws BodyParser.BodyParseException {
        String json = """
                {
                  "name": "John",
                  "email": null,
                  "role": "admin"
                }
                """;

        Map<String, Object> result = parser.parse(json);

        assertEquals("John", result.get("name"));
        assertNull(result.get("email"));
        assertEquals("admin", result.get("role"));
    }

    @Test
    @DisplayName("Preserves numeric types")
    void testNumericTypes() throws BodyParser.BodyParseException {
        String json = """
                {
                  "intValue": 42,
                  "floatValue": 3.14,
                  "boolValue": true
                }
                """;

        Map<String, Object> result = parser.parse(json);

        assertEquals(42, result.get("intValue"));
        assertEquals(3.14, result.get("floatValue"));
        assertEquals(true, result.get("boolValue"));
    }

    @Test
    @DisplayName("Empty body returns empty map")
    void testEmptyBody() throws BodyParser.BodyParseException {
        Map<String, Object> result = parser.parse("");
        assertTrue(result.isEmpty());

        result = parser.parse(null);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Malformed JSON throws BodyParseException")
    void testMalformedJson() {
        String malformedJson = "{\"name\": \"John\", invalid}";

        assertThrows(BodyParser.BodyParseException.class, () -> parser.parse(malformedJson));
    }

    @Test
    @DisplayName("OAuth2 authentication request (typical use case)")
    void testOAuth2Request() throws BodyParser.BodyParseException {
        String json = """
                {
                  "username": "karthi",
                  "password": "Welcome@01",
                  "rememberMe": true
                }
                """;

        Map<String, Object> result = parser.parse(json);

        assertEquals("karthi", result.get("username"));
        assertEquals("Welcome@01", result.get("password"));
        assertEquals(true, result.get("rememberMe"));
    }

    @Test
    @DisplayName("REST API order with nested arrays (typical use case)")
    void testOrderRequest() throws BodyParser.BodyParseException {
        String json = """
                {
                  "customerId": 100,
                  "items": [
                    {"id": 1, "quantity": 2, "price": 50},
                    {"id": 2, "quantity": 1, "price": 100}
                  ]
                }
                """;

        Map<String, Object> result = parser.parse(json);

        assertEquals(100, result.get("customerId"));
        assertEquals(1, result.get("items[0].id"));
        assertEquals(2, result.get("items[0].quantity"));
        assertEquals(50, result.get("items[0].price"));
        assertEquals(2, result.get("items[1].id"));
        assertEquals(1, result.get("items[1].quantity"));
        assertEquals(100, result.get("items[1].price"));
    }
}

package com.mockr.runnr.matcher.body;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FormBodyParser - Form-URLEncoded Parsing")
class FormBodyParserTest {

    private final FormBodyParser parser = new FormBodyParser();

    @Test
    @DisplayName("Simple key-value form parsing")
    void testSimpleForm() throws BodyParser.BodyParseException {
        String form = "username=admin&password=Pass123&role=user";

        Map<String, Object> result = parser.parse(form);

        assertEquals("admin", result.get("username"));
        assertEquals("Pass123", result.get("password"));
        assertEquals("user", result.get("role"));
    }

    @Test
    @DisplayName("URL-encoded values are decoded")
    void testUrlEncoding() throws BodyParser.BodyParseException {
        String form = "email=john%40example.com&message=Hello%20World";

        Map<String, Object> result = parser.parse(form);

        assertEquals("john@example.com", result.get("email"));
        assertEquals("Hello World", result.get("message"));
    }

    @Test
    @DisplayName("OAuth2 token request (typical use case)")
    void testOAuth2TokenRequest() throws BodyParser.BodyParseException {
        String form = "grant_type=password&username=admin&password=Pass123&scope=read";

        Map<String, Object> result = parser.parse(form);

        assertEquals("password", result.get("grant_type"));
        assertEquals("admin", result.get("username"));
        assertEquals("Pass123", result.get("password"));
        assertEquals("read", result.get("scope"));
    }

    @Test
    @DisplayName("Empty values are supported")
    void testEmptyValues() throws BodyParser.BodyParseException {
        String form = "name=John&email=&phone=123";

        Map<String, Object> result = parser.parse(form);

        assertEquals("John", result.get("name"));
        assertEquals("", result.get("email"));
        assertEquals("123", result.get("phone"));
    }

    @Test
    @DisplayName("Single parameter")
    void testSingleParameter() throws BodyParser.BodyParseException {
        String form = "username=admin";

        Map<String, Object> result = parser.parse(form);

        assertEquals("admin", result.get("username"));
        assertEquals(1, result.size());
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
    @DisplayName("Keys are lowercased")
    void testKeyLowercasing() throws BodyParser.BodyParseException {
        String form = "UserName=admin&PassWord=Pass123&ROLE=user";

        Map<String, Object> result = parser.parse(form);

        assertEquals("admin", result.get("username"));
        assertEquals("Pass123", result.get("password"));
        assertEquals("user", result.get("role"));
    }

    @Test
    @DisplayName("Values with equals signs are handled correctly")
    void testValueWithEqualsSign() throws BodyParser.BodyParseException {
        String form = "jwt=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0=";

        Map<String, Object> result = parser.parse(form);

        assertEquals("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0=", result.get("jwt"));
    }

    @Test
    @DisplayName("Multiple values for same key (last wins)")
    void testDuplicateKeys() throws BodyParser.BodyParseException {
        String form = "role=user&role=admin";

        Map<String, Object> result = parser.parse(form);

        // Last value should win
        assertEquals("admin", result.get("role"));
    }

    @Test
    @DisplayName("Whitespace handling")
    void testWhitespaceHandling() throws BodyParser.BodyParseException {
        String form = "name=John%20Doe&title=Senior%20Developer";

        Map<String, Object> result = parser.parse(form);

        assertEquals("John Doe", result.get("name"));
        assertEquals("Senior Developer", result.get("title"));
    }
}

package com.mockr.runnr.matcher.body;

import com.mockr.runnr.enums.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BodyParserRegistry - Parser Selection by Content-Type")
class BodyParserRegistryTest {

    @Test
    @DisplayName("JSON parser selected for application/json")
    void testJsonParserSelection() {
        BodyParser parser = BodyParserRegistry.selectParser("application/json");
        assertInstanceOf(JsonBodyParser.class, parser);
    }

    @Test
    @DisplayName("JSON parser selected with charset parameter")
    void testJsonWithCharset() {
        BodyParser parser = BodyParserRegistry.selectParser("application/json; charset=utf-8");
        assertInstanceOf(JsonBodyParser.class, parser);
    }

    @Test
    @DisplayName("Form parser selected for application/x-www-form-urlencoded")
    void testFormParserSelection() {
        BodyParser parser = BodyParserRegistry.selectParser("application/x-www-form-urlencoded");
        assertInstanceOf(FormBodyParser.class, parser);
    }

    @Test
    @DisplayName("XML parser selected for application/xml")
    void testXmlParserSelection() {
        BodyParser parser = BodyParserRegistry.selectParser("application/xml");
        assertInstanceOf(XmlBodyParser.class, parser);
    }

    @Test
    @DisplayName("XML parser selected for application/soap+xml")
    void testSoapParserSelection() {
        BodyParser parser = BodyParserRegistry.selectParser("application/soap+xml");
        assertInstanceOf(XmlBodyParser.class, parser);
    }

    @Test
    @DisplayName("XML parser selected for text/xml")
    void testTextXmlParserSelection() {
        BodyParser parser = BodyParserRegistry.selectParser("text/xml");
        assertInstanceOf(XmlBodyParser.class, parser);
    }

    @Test
    @DisplayName("Raw parser selected for unknown content type")
    void testUnknownContentType() {
        BodyParser parser = BodyParserRegistry.selectParser("application/custom");
        assertInstanceOf(RawBodyParser.class, parser);
    }

    @Test
    @DisplayName("Raw parser selected for null content type")
    void testNullContentType() {
        BodyParser parser = BodyParserRegistry.selectParser((String) null);
        assertInstanceOf(RawBodyParser.class, parser);
    }

    @Test
    @DisplayName("Raw parser selected for empty content type")
    void testEmptyContentType() {
        BodyParser parser = BodyParserRegistry.selectParser("");
        assertInstanceOf(RawBodyParser.class, parser);
    }

    @Test
    @DisplayName("Content-Type is case-insensitive")
    void testCaseInsensitivity() {
        BodyParser parser1 = BodyParserRegistry.selectParser("APPLICATION/JSON");
        BodyParser parser2 = BodyParserRegistry.selectParser("application/json");

        assertInstanceOf(JsonBodyParser.class, parser1);
        assertInstanceOf(JsonBodyParser.class, parser2);
    }

    @Test
    @DisplayName("Whitespace in content-type is trimmed")
    void testWhitespaceTrimming() {
        BodyParser parser = BodyParserRegistry.selectParser("  application/json  ");
        assertInstanceOf(JsonBodyParser.class, parser);
    }

    @Nested
    @DisplayName("selectParser(ContentType) - Enum overload")
    class SelectParserWithContentTypeEnumTests {

        @Test
        void shouldReturnJsonParserForApplicationJson() {
            BodyParser parser = BodyParserRegistry.selectParser(ContentType.APPLICATION_JSON);
            assertNotNull(parser);
            assertInstanceOf(JsonBodyParser.class, parser);
        }

        @Test
        void shouldReturnXmlParserForApplicationXml() {
            BodyParser parser = BodyParserRegistry.selectParser(ContentType.APPLICATION_XML);
            assertNotNull(parser);
            assertInstanceOf(XmlBodyParser.class, parser);
        }

        @Test
        void shouldReturnFormParserForFormUrlEncoded() {
            BodyParser parser = BodyParserRegistry.selectParser(ContentType.APPLICATION_FORM_URLENCODED);
            assertNotNull(parser);
            assertInstanceOf(FormBodyParser.class, parser);
        }

        @Test
        void shouldReturnRawParserForTextPlain() {
            BodyParser parser = BodyParserRegistry.selectParser(ContentType.TEXT_PLAIN);
            assertNotNull(parser);
            assertInstanceOf(RawBodyParser.class, parser);
        }

        @Test
        void shouldReturnDefaultParserForNull() {
            BodyParser parser = BodyParserRegistry.selectParser((ContentType) null);
            assertNotNull(parser);
            assertInstanceOf(RawBodyParser.class, parser);
        }
    }

    @Nested
    @DisplayName("selectParserForEndpoint - Endpoint-aware with fallback")
    class SelectParserForEndpointTests {

        @Test
        @DisplayName("Endpoint config takes priority over header")
        void shouldUseEndpointContentTypePrimary() {
            // Endpoint says JSON, header says XML
            BodyParser parser = BodyParserRegistry.selectParserForEndpoint(
                    ContentType.APPLICATION_JSON,
                    "application/xml");
            assertNotNull(parser);
            assertInstanceOf(JsonBodyParser.class, parser);
        }

        @Test
        @DisplayName("Fallback to header when endpoint is null")
        void shouldFallbackToHeaderWhenEndpointNull() {
            // Endpoint not specified, use header
            BodyParser parser = BodyParserRegistry.selectParserForEndpoint(
                    null,
                    "application/xml");
            assertNotNull(parser);
            assertInstanceOf(XmlBodyParser.class, parser);
        }

        @Test
        @DisplayName("Default parser when both endpoint and header are null")
        void shouldFallbackToDefaultWhenBothNull() {
            // Neither endpoint nor header specified
            BodyParser parser = BodyParserRegistry.selectParserForEndpoint(
                    null,
                    null);
            assertNotNull(parser);
            assertInstanceOf(RawBodyParser.class, parser);
        }

        @Test
        @DisplayName("Endpoint overrides header with charset")
        void shouldUseEndpointOverHeaderWithCharset() {
            // Endpoint specifies XML, header says JSON with charset
            BodyParser parser = BodyParserRegistry.selectParserForEndpoint(
                    ContentType.APPLICATION_XML,
                    "application/json; charset=utf-8");
            assertNotNull(parser);
            assertInstanceOf(XmlBodyParser.class, parser);
        }

        @Test
        @DisplayName("Header with charset used as fallback")
        void shouldFallbackToHeaderWithCharset() {
            // Endpoint not specified, use header with charset
            BodyParser parser = BodyParserRegistry.selectParserForEndpoint(
                    null,
                    "application/json; charset=utf-8");
            assertNotNull(parser);
            assertInstanceOf(JsonBodyParser.class, parser);
        }

        @Test
        @DisplayName("Default parser when header is blank and endpoint is null")
        void shouldHandleBlankHeaderWhenEndpointNull() {
            // Endpoint null, header blank
            BodyParser parser = BodyParserRegistry.selectParserForEndpoint(
                    null,
                    "");
            assertNotNull(parser);
            assertInstanceOf(RawBodyParser.class, parser);
        }
    }
}

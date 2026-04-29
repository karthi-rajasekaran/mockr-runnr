package com.mockr.runnr.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ContentType Enum Tests")
class ContentTypeTest {

    @Nested
    @DisplayName("getMediaType")
    class GetMediaTypeTests {

        @Test
        void shouldReturnCorrectMediaTypeForJson() {
            assertEquals("application/json", ContentType.APPLICATION_JSON.getMediaType());
        }

        @Test
        void shouldReturnCorrectMediaTypeForXml() {
            assertEquals("application/xml", ContentType.APPLICATION_XML.getMediaType());
        }

        @Test
        void shouldReturnCorrectMediaTypeForFormUrlEncoded() {
            assertEquals("application/x-www-form-urlencoded",
                    ContentType.APPLICATION_FORM_URLENCODED.getMediaType());
        }

        @Test
        void shouldReturnCorrectMediaTypeForYaml() {
            assertEquals("application/yaml", ContentType.APPLICATION_YAML.getMediaType());
        }
    }

    @Nested
    @DisplayName("fromMediaType")
    class FromMediaTypeTests {

        @Test
        void shouldFindJsonFromExactMediaType() {
            assertEquals(ContentType.APPLICATION_JSON,
                    ContentType.fromMediaType("application/json"));
        }

        @Test
        void shouldFindJsonFromMediaTypeWithCharset() {
            assertEquals(ContentType.APPLICATION_JSON,
                    ContentType.fromMediaType("application/json; charset=utf-8"));
        }

        @Test
        void shouldBeCaseInsensitive() {
            assertEquals(ContentType.APPLICATION_JSON,
                    ContentType.fromMediaType("APPLICATION/JSON"));
        }

        @Test
        void shouldHandleMultipleParameters() {
            assertEquals(ContentType.APPLICATION_JSON,
                    ContentType.fromMediaType("application/json; charset=utf-8; boundary=something"));
        }

        @Test
        void shouldFindXmlFromMediaType() {
            assertEquals(ContentType.APPLICATION_XML,
                    ContentType.fromMediaType("application/xml"));
        }

        @Test
        void shouldFindFormUrlEncodedFromMediaType() {
            assertEquals(ContentType.APPLICATION_FORM_URLENCODED,
                    ContentType.fromMediaType("application/x-www-form-urlencoded"));
        }

        @Test
        void shouldReturnNullForUnknownMediaType() {
            assertNull(ContentType.fromMediaType("application/unknown"));
        }

        @Test
        void shouldReturnNullForNull() {
            assertNull(ContentType.fromMediaType(null));
        }

        @Test
        void shouldReturnNullForBlank() {
            assertNull(ContentType.fromMediaType(""));
            assertNull(ContentType.fromMediaType("   "));
        }

        @Test
        void shouldTrimWhitespace() {
            assertEquals(ContentType.APPLICATION_JSON,
                    ContentType.fromMediaType("  application/json  "));
        }
    }
}

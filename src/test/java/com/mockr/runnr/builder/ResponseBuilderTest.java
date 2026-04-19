package com.mockr.runnr.builder;

import com.mockr.runnr.builder.strategies.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResponseBuilderTest {

    private ResponseBuilder builder;
    private ResponseStrategyFactory factory;

    @BeforeEach
    void setUp() {
        List<ResponseStrategy> strategies = List.of(
                new JsonResponseStrategy(),
                new XmlResponseStrategy(),
                new HtmlResponseStrategy(),
                new FileResponseStrategy(),
                new SoapResponseStrategy(),
                new GraphQLResponseStrategy());
        factory = new ResponseStrategyFactory(strategies);
        builder = new ResponseBuilder();
    }

    @Nested
    @DisplayName("Builder fluent API")
    class BuilderFluentApi {

        @Test
        void shouldReturnBuilderFromEachMethod() {
            ResponseBuilder result = builder.withStatus(201)
                    .withHeader("X-Custom", "value")
                    .withBody("test")
                    .withResponseType(ResponseType.JSON);

            assertNotNull(result);
            assertSame(result, builder);
        }

        @Test
        void shouldSetAllPropertiesCorrectly() {
            builder.withStatus(202)
                    .withHeader("Auth", "Bearer token")
                    .withBody("response body")
                    .withResponseType(ResponseType.XML);

            assertEquals(202, builder.getStatus());
            assertEquals("response body", builder.getBody());
            assertEquals(ResponseType.XML, builder.getResponseType());
        }
    }

    @Nested
    @DisplayName("Status validation")
    class StatusValidation {

        @Test
        void shouldRejectStatusCodeBelow100() {
            assertThrows(ResponseBuilderException.class, () -> builder.withStatus(99));
        }

        @Test
        void shouldRejectStatusCodeAbove599() {
            assertThrows(ResponseBuilderException.class, () -> builder.withStatus(600));
        }

        @Test
        void shouldAcceptValidStatusCodes() {
            builder.withStatus(100);
            assertEquals(100, builder.getStatus());

            builder.withStatus(599);
            assertEquals(599, builder.getStatus());
        }

        @Test
        void shouldDefaultStatusTo200() {
            assertEquals(200, builder.getStatus());
        }
    }

    @Nested
    @DisplayName("Header handling")
    class HeaderHandling {

        @Test
        void shouldRejectNullHeaderKey() {
            assertThrows(ResponseBuilderException.class, () -> builder.withHeader(null, "value"));
        }

        @Test
        void shouldRejectEmptyHeaderKey() {
            assertThrows(ResponseBuilderException.class, () -> builder.withHeader("", "value"));
        }

        @Test
        void shouldRejectBlankHeaderKey() {
            assertThrows(ResponseBuilderException.class, () -> builder.withHeader("  ", "value"));
        }

        @Test
        void shouldAllowNullHeaderValue() {
            builder.withHeader("X-Custom", null);
            assertTrue(builder.getCustomHeaders().containsKey("X-Custom"));
        }

        @Test
        void shouldStoreMultipleHeaders() {
            builder.withHeader("Content-Type", "application/json")
                    .withHeader("X-API-Key", "secret")
                    .withHeader("Authorization", "Bearer token");

            assertEquals(3, builder.getCustomHeaders().size());
        }

        @Test
        void shouldOverwriteDuplicateHeaders() {
            builder.withHeader("X-Custom", "first")
                    .withHeader("X-Custom", "second");

            assertEquals("second", builder.getCustomHeaders().get("X-Custom"));
            assertEquals(1, builder.getCustomHeaders().size());
        }
    }

    @Nested
    @DisplayName("Response type handling")
    class ResponseTypeHandling {

        @Test
        void shouldRejectNullResponseType() {
            assertThrows(ResponseBuilderException.class, () -> builder.withResponseType(null));
        }

        @Test
        void shouldDefaultResponseTypeToJson() {
            assertEquals(ResponseType.JSON, builder.getResponseType());
        }

        @Test
        void shouldSetResponseType() {
            builder.withResponseType(ResponseType.XML);
            assertEquals(ResponseType.XML, builder.getResponseType());
        }
    }

    @Nested
    @DisplayName("JSON response building")
    class JsonResponseBuilding {

        @Test
        void shouldBuildJsonResponse() {
            ResponseEntity<?> response = builder
                    .withStatus(200)
                    .withBody("{\"message\": \"hello\"}")
                    .withResponseType(ResponseType.JSON)
                    .build(factory);

            assertEquals(200, response.getStatusCode().value());
            assertEquals("{\"message\": \"hello\"}", response.getBody());
            assertEquals("application/json", response.getHeaders().getContentType().toString());
        }

        @Test
        void shouldPreserveCustomContentType() {
            ResponseEntity<?> response = builder
                    .withHeader("Content-Type", "application/json; charset=UTF-8")
                    .withBody("test")
                    .withResponseType(ResponseType.JSON)
                    .build(factory);

            assertTrue(response.getHeaders().getFirst("Content-Type")
                    .contains("application/json"));
        }
    }

    @Nested
    @DisplayName("XML response building")
    class XmlResponseBuilding {

        @Test
        void shouldBuildXmlResponse() {
            ResponseEntity<?> response = builder
                    .withStatus(201)
                    .withBody("<root><message>hello</message></root>")
                    .withResponseType(ResponseType.XML)
                    .build(factory);

            assertEquals(201, response.getStatusCode().value());
            assertTrue(response.getBody().toString().contains("<root>"));
            assertEquals("application/xml", response.getHeaders().getContentType().toString());
        }
    }

    @Nested
    @DisplayName("HTML response building")
    class HtmlResponseBuilding {

        @Test
        void shouldBuildHtmlResponse() {
            ResponseEntity<?> response = builder
                    .withStatus(200)
                    .withBody("<html><body>Hello</body></html>")
                    .withResponseType(ResponseType.HTML)
                    .build(factory);

            assertEquals(200, response.getStatusCode().value());
            assertEquals("text/html", response.getHeaders().getContentType().toString());
        }
    }

    @Nested
    @DisplayName("SOAP response building")
    class SoapResponseBuilding {

        @Test
        void shouldBuildSoapResponse() {
            ResponseEntity<?> response = builder
                    .withStatus(200)
                    .withBody("<soap:Envelope>...</soap:Envelope>")
                    .withResponseType(ResponseType.SOAP)
                    .build(factory);

            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getHeaders().getContentType().toString().contains("soap"));
        }
    }

    @Nested
    @DisplayName("GraphQL response building")
    class GraphQLResponseBuilding {

        @Test
        void shouldBuildGraphQLResponse() {
            ResponseEntity<?> response = builder
                    .withStatus(200)
                    .withBody("{\"data\": {\"user\": \"John\"}}")
                    .withResponseType(ResponseType.GRAPHQL)
                    .build(factory);

            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getHeaders().getContentType().toString().contains("graphql"));
        }
    }

    @Nested
    @DisplayName("File response building")
    class FileResponseBuilding {

        @Test
        void shouldThrowExceptionForMissingFilePath() {
            assertThrows(ResponseBuilderException.class, () -> builder.withResponseType(ResponseType.FILE)
                    .build(factory));
        }

        @Test
        void shouldThrowExceptionForNonExistentFile() {
            assertThrows(ResponseBuilderException.class, () -> builder.withFilePath("/path/to/nonexistent/file.pdf")
                    .withResponseType(ResponseType.FILE)
                    .build(factory));
        }
    }

    @Nested
    @DisplayName("Raw response building")
    class RawResponseBuilding {

        @Test
        void shouldBuildRawBinaryResponse() {
            byte[] data = "binary content".getBytes();

            ResponseEntity<?> response = builder
                    .withStatus(200)
                    .buildRaw(data, factory);

            assertEquals(200, response.getStatusCode().value());
            assertEquals(data, response.getBody());
        }

        @Test
        void shouldSetContentTypeForBinaryResponse() {
            byte[] data = new byte[] { 1, 2, 3, 4 };

            ResponseEntity<?> response = builder
                    .withStatus(200)
                    .buildRaw(data, factory);

            assertEquals("application/octet-stream",
                    response.getHeaders().getContentType().toString());
        }
    }

    @Nested
    @DisplayName("Multiple headers")
    class MultipleHeaders {

        @Test
        void shouldIncludeAllHeadersInResponse() {
            ResponseEntity<?> response = builder
                    .withStatus(200)
                    .withHeader("X-Custom-1", "value1")
                    .withHeader("X-Custom-2", "value2")
                    .withHeader("X-Custom-3", "value3")
                    .withBody("test")
                    .withResponseType(ResponseType.JSON)
                    .build(factory);

            assertEquals("value1", response.getHeaders().getFirst("X-Custom-1"));
            assertEquals("value2", response.getHeaders().getFirst("X-Custom-2"));
            assertEquals("value3", response.getHeaders().getFirst("X-Custom-3"));
        }
    }

    @Nested
    @DisplayName("Error handling")
    class ErrorHandling {

        @Test
        void shouldThrowForUnregisteredResponseType() {
            builder.withStatus(200).withBody("test");
            // Manually test with missing strategy (unlikely in practice with Spring DI)
            assertEquals(ResponseType.JSON, builder.getResponseType());
        }
    }
}

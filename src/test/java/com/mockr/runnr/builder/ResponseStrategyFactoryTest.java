package com.mockr.runnr.builder;

import com.mockr.runnr.builder.strategies.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResponseStrategyFactoryTest {

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
    }

    @Test
    void shouldReturnJsonStrategy() {
        ResponseStrategy strategy = factory.getStrategy(ResponseType.JSON);
        assertNotNull(strategy);
        assertEquals("application/json", strategy.getContentType());
    }

    @Test
    void shouldReturnXmlStrategy() {
        ResponseStrategy strategy = factory.getStrategy(ResponseType.XML);
        assertNotNull(strategy);
        assertEquals("application/xml", strategy.getContentType());
    }

    @Test
    void shouldReturnHtmlStrategy() {
        ResponseStrategy strategy = factory.getStrategy(ResponseType.HTML);
        assertNotNull(strategy);
        assertEquals("text/html", strategy.getContentType());
    }

    @Test
    void shouldReturnFileStrategy() {
        ResponseStrategy strategy = factory.getStrategy(ResponseType.FILE);
        assertNotNull(strategy);
        assertEquals("application/octet-stream", strategy.getContentType());
    }

    @Test
    void shouldReturnSoapStrategy() {
        ResponseStrategy strategy = factory.getStrategy(ResponseType.SOAP);
        assertNotNull(strategy);
        assertEquals("application/soap+xml", strategy.getContentType());
    }

    @Test
    void shouldReturnGraphQLStrategy() {
        ResponseStrategy strategy = factory.getStrategy(ResponseType.GRAPHQL);
        assertNotNull(strategy);
        assertEquals("application/graphql+json", strategy.getContentType());
    }

    @Test
    void shouldThrowForMissingStrategy() {
        // Simulate by creating factory with empty strategies
        factory = new ResponseStrategyFactory(List.of());
        assertThrows(ResponseBuilderException.class,
                () -> factory.getStrategy(ResponseType.JSON));
    }
}

package com.mockr.runnr.builder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ResponseStrategyFactory {

    private final Map<ResponseType, ResponseStrategy> strategyMap;

    public ResponseStrategyFactory(List<ResponseStrategy> strategies) {
        this.strategyMap = new EnumMap<>(ResponseType.class);
        registerStrategies(strategies);
        log.info("ResponseStrategyFactory initialized with {} response strategies", strategyMap.size());
    }

    private void registerStrategies(List<ResponseStrategy> strategies) {
        // Map strategies by their content type
        for (ResponseStrategy strategy : strategies) {
            String contentType = strategy.getContentType();
            ResponseType type = mapContentTypeToResponseType(contentType);
            if (type != null) {
                strategyMap.put(type, strategy);
            }
        }
    }

    private ResponseType mapContentTypeToResponseType(String contentType) {
        return switch (contentType) {
            case "application/json" -> ResponseType.JSON;
            case "application/xml" -> ResponseType.XML;
            case "text/html" -> ResponseType.HTML;
            case "application/octet-stream" -> ResponseType.FILE;
            case "application/soap+xml" -> ResponseType.SOAP;
            case "application/graphql+json" -> ResponseType.GRAPHQL;
            default -> null;
        };
    }

    public ResponseStrategy getStrategy(ResponseType responseType) {
        ResponseStrategy strategy = strategyMap.get(responseType);
        if (strategy == null) {
            throw new ResponseBuilderException(
                    "No strategy registered for response type: " + responseType);
        }
        return strategy;
    }
}

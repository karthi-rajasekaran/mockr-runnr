package com.mockr.runnr.matcher;

import com.mockr.runnr.domain.ConditionOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class OperatorFactory {
    private final Map<ConditionOperator, ConditionOperatorStrategy> strategyMap;

    public OperatorFactory(List<ConditionOperatorStrategy> strategies) {
        this.strategyMap = new EnumMap<>(ConditionOperator.class);
        for (ConditionOperatorStrategy strategy : strategies) {
            strategyMap.put(strategy.getOperator(), strategy);
        }
        log.info("OperatorFactory initialized with {} operator strategies", strategyMap.size());
    }

    public ConditionOperatorStrategy getOperator(ConditionOperator operator) {
        ConditionOperatorStrategy strategy = strategyMap.get(operator);
        if (strategy == null) {
            throw new UnsupportedOperationException("No strategy registered for operator: " + operator);
        }
        return strategy;
    }
}

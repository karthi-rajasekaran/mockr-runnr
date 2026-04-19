package com.mockr.runnr.matcher.operators;

import com.mockr.runnr.domain.ConditionOperator;
import com.mockr.runnr.matcher.ConditionOperatorStrategy;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class InOperator implements ConditionOperatorStrategy {

    @Override
    public boolean evaluate(String actual, String expected) {
        if (actual == null || expected == null) {
            return false;
        }
        Set<String> values = Arrays.stream(expected.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());
        return values.contains(actual);
    }

    @Override
    public ConditionOperator getOperator() {
        return ConditionOperator.IN;
    }
}

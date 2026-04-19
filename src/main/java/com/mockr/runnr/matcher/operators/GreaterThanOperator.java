package com.mockr.runnr.matcher.operators;

import com.mockr.runnr.domain.ConditionOperator;
import com.mockr.runnr.matcher.ConditionOperatorStrategy;
import org.springframework.stereotype.Component;

@Component
public class GreaterThanOperator implements ConditionOperatorStrategy {

    @Override
    public boolean evaluate(String actual, String expected) {
        if (actual == null || expected == null) {
            return false;
        }
        try {
            return Double.parseDouble(actual) > Double.parseDouble(expected);
        } catch (NumberFormatException e) {
            return actual.compareTo(expected) > 0;
        }
    }

    @Override
    public ConditionOperator getOperator() {
        return ConditionOperator.GT;
    }
}

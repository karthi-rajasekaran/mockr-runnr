package com.mockr.runnr.matcher.operators;

import com.mockr.runnr.domain.ConditionOperator;
import com.mockr.runnr.matcher.ConditionOperatorStrategy;
import org.springframework.stereotype.Component;

@Component
public class EqualityOperator implements ConditionOperatorStrategy {

    @Override
    public boolean evaluate(String actual, String expected) {
        if (actual == null || expected == null) {
            return actual == null && expected == null;
        }
        return actual.equals(expected);
    }

    @Override
    public ConditionOperator getOperator() {
        return ConditionOperator.EQ;
    }
}

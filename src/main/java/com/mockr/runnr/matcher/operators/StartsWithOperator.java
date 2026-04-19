package com.mockr.runnr.matcher.operators;

import com.mockr.runnr.domain.ConditionOperator;
import com.mockr.runnr.matcher.ConditionOperatorStrategy;
import org.springframework.stereotype.Component;

@Component
public class StartsWithOperator implements ConditionOperatorStrategy {

    @Override
    public boolean evaluate(String actual, String expected) {
        if (actual == null || expected == null) {
            return false;
        }
        return actual.startsWith(expected);
    }

    @Override
    public ConditionOperator getOperator() {
        return ConditionOperator.STARTS_WITH;
    }
}

package com.mockr.runnr.matcher;

import com.mockr.runnr.domain.ConditionOperator;

public interface ConditionOperatorStrategy {

    boolean evaluate(String actual, String expected);

    ConditionOperator getOperator();
}

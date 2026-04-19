package com.mockr.runnr.matcher.operators;

import com.mockr.runnr.domain.ConditionOperator;
import com.mockr.runnr.matcher.ConditionOperatorStrategy;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Component
public class RegexOperator implements ConditionOperatorStrategy {

    @Override
    public boolean evaluate(String actual, String expected) {
        if (actual == null || expected == null) {
            return false;
        }
        try {
            return Pattern.matches(expected, actual);
        } catch (PatternSyntaxException e) {
            return false;
        }
    }

    @Override
    public ConditionOperator getOperator() {
        return ConditionOperator.REGEX;
    }
}

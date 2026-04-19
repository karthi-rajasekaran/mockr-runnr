package com.mockr.runnr.matcher;

import com.mockr.runnr.domain.ConditionOperator;
import com.mockr.runnr.matcher.operators.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OperatorFactoryTest {

    private OperatorFactory factory;

    @BeforeEach
    void setUp() {
        List<ConditionOperatorStrategy> strategies = List.of(
                new EqualityOperator(),
                new NotEqualOperator(),
                new GreaterThanOperator(),
                new LessThanOperator(),
                new ContainsOperator(),
                new InOperator());
        factory = new OperatorFactory(strategies);
    }

    @Test
    void shouldReturnCorrectStrategyForRegisteredOperator() {
        ConditionOperatorStrategy strategy = factory.getOperator(ConditionOperator.EQ);
        assertNotNull(strategy);
        assertEquals(ConditionOperator.EQ, strategy.getOperator());
    }

    @Test
    void shouldThrowForUnregisteredOperator() {
        assertThrows(UnsupportedOperationException.class,
                () -> factory.getOperator(ConditionOperator.REGEX));
    }
}

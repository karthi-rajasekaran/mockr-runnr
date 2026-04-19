package com.mockr.runnr.matcher;

import com.mockr.runnr.domain.Condition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConditionEvaluator {

    private final OperatorFactory operatorFactory;

    public boolean evaluate(Collection<Condition> conditions, EvaluationContext context) {
        if (conditions == null || conditions.isEmpty()) {
            log.debug("No conditions to evaluate, returning true");
            return true;
        }

        for (Condition condition : conditions) {
            if (!evaluateSingle(condition, context)) {
                log.debug("Condition evaluation failed, short-circuiting: lhs={}, op={}, rhs={}",
                        condition.getLhs(), condition.getOperation(), condition.getRhs());
                return false;
            }
        }

        log.debug("All {} conditions matched", conditions.size());
        return true;
    }

    public boolean evaluateSingle(Condition condition, EvaluationContext context) {
        String lhs = condition.getLhs();
        String actualValue = context.resolve(lhs);
        String expectedValue = condition.getRhs();

        ConditionOperatorStrategy strategy = operatorFactory.getOperator(condition.getOperation());
        boolean result = strategy.evaluate(actualValue, expectedValue);

        log.debug("Condition: [{}] {} [{}] | actual=[{}] -> {}",
                lhs, condition.getOperation(), expectedValue, actualValue, result);

        return result;
    }
}

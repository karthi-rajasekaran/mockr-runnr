package com.mockr.runnr.matcher;

import com.mockr.runnr.domain.Condition;
import com.mockr.runnr.domain.ConditionOperator;
import com.mockr.runnr.matcher.operators.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConditionEvaluatorTest {

    private ConditionEvaluator evaluator;

    @BeforeEach
    void setUp() {
        List<ConditionOperatorStrategy> strategies = List.of(
                new EqualityOperator(),
                new NotEqualOperator(),
                new GreaterThanOperator(),
                new LessThanOperator(),
                new GreaterThanOrEqualOperator(),
                new LessThanOrEqualOperator(),
                new ContainsOperator(),
                new NotContainsOperator(),
                new InOperator(),
                new NotInOperator(),
                new RegexOperator(),
                new StartsWithOperator(),
                new EndsWithOperator());
        OperatorFactory factory = new OperatorFactory(strategies);
        evaluator = new ConditionEvaluator(factory);
    }

    private Condition buildCondition(String lhs, ConditionOperator op, String rhs) {
        return Condition.builder()
                .lhs(lhs)
                .operation(op)
                .rhs(rhs)
                .build();
    }

    private EvaluationContext contextWith(Map<String, String> headers,
            Map<String, String> queryParams,
            Map<String, String> pathVars) {
        return EvaluationContext.builder()
                .headers(headers)
                .queryParameters(queryParams)
                .pathVariables(pathVars)
                .build();
    }

    @Nested
    @DisplayName("Empty/Null conditions")
    class EmptyConditions {

        @Test
        void shouldReturnTrueForNullConditions() {
            EvaluationContext ctx = contextWith(Map.of(), Map.of(), Map.of());
            assertTrue(evaluator.evaluate(null, ctx));
        }

        @Test
        void shouldReturnTrueForEmptyConditions() {
            EvaluationContext ctx = contextWith(Map.of(), Map.of(), Map.of());
            assertTrue(evaluator.evaluate(Collections.emptyList(), ctx));
        }
    }

    @Nested
    @DisplayName("Header extraction")
    class HeaderExtraction {

        @Test
        void shouldExtractHeaderValueCaseInsensitive() {
            Condition condition = buildCondition("header.X-API-Key", ConditionOperator.EQ, "abc123");
            EvaluationContext ctx = contextWith(Map.of("x-api-key", "abc123"), Map.of(), Map.of());
            assertTrue(evaluator.evaluateSingle(condition, ctx));
        }

        @Test
        void shouldReturnFalseForMissingHeader() {
            Condition condition = buildCondition("header.X-Missing", ConditionOperator.EQ, "value");
            EvaluationContext ctx = contextWith(Map.of(), Map.of(), Map.of());
            assertFalse(evaluator.evaluateSingle(condition, ctx));
        }
    }

    @Nested
    @DisplayName("Query parameter extraction")
    class QueryParamExtraction {

        @Test
        void shouldExtractQueryParam() {
            Condition condition = buildCondition("query.limit", ConditionOperator.EQ, "10");
            EvaluationContext ctx = contextWith(Map.of(), Map.of("limit", "10"), Map.of());
            assertTrue(evaluator.evaluateSingle(condition, ctx));
        }

        @Test
        void shouldReturnFalseForMissingQueryParam() {
            Condition condition = buildCondition("query.missing", ConditionOperator.EQ, "val");
            EvaluationContext ctx = contextWith(Map.of(), Map.of(), Map.of());
            assertFalse(evaluator.evaluateSingle(condition, ctx));
        }
    }

    @Nested
    @DisplayName("Path variable extraction")
    class PathVariableExtraction {

        @Test
        void shouldExtractPathVariable() {
            Condition condition = buildCondition("path.id", ConditionOperator.EQ, "42");
            EvaluationContext ctx = contextWith(Map.of(), Map.of(), Map.of("id", "42"));
            assertTrue(evaluator.evaluateSingle(condition, ctx));
        }
    }

    @Nested
    @DisplayName("EQ operator")
    class EqOperator {

        @Test
        void shouldMatchEqual() {
            Condition condition = buildCondition("header.token", ConditionOperator.EQ, "abc");
            EvaluationContext ctx = contextWith(Map.of("token", "abc"), Map.of(), Map.of());
            assertTrue(evaluator.evaluateSingle(condition, ctx));
        }

        @Test
        void shouldNotMatchDifferent() {
            Condition condition = buildCondition("header.token", ConditionOperator.EQ, "abc");
            EvaluationContext ctx = contextWith(Map.of("token", "xyz"), Map.of(), Map.of());
            assertFalse(evaluator.evaluateSingle(condition, ctx));
        }
    }

    @Nested
    @DisplayName("NEQ operator")
    class NeqOperator {

        @Test
        void shouldMatchWhenNotEqual() {
            Condition condition = buildCondition("query.status", ConditionOperator.NEQ, "active");
            EvaluationContext ctx = contextWith(Map.of(), Map.of("status", "inactive"), Map.of());
            assertTrue(evaluator.evaluateSingle(condition, ctx));
        }

        @Test
        void shouldNotMatchWhenEqual() {
            Condition condition = buildCondition("query.status", ConditionOperator.NEQ, "active");
            EvaluationContext ctx = contextWith(Map.of(), Map.of("status", "active"), Map.of());
            assertFalse(evaluator.evaluateSingle(condition, ctx));
        }
    }

    @Nested
    @DisplayName("GT operator")
    class GtOperator {

        @Test
        void shouldMatchGreaterNumeric() {
            Condition condition = buildCondition("query.age", ConditionOperator.GT, "18");
            EvaluationContext ctx = contextWith(Map.of(), Map.of("age", "25"), Map.of());
            assertTrue(evaluator.evaluateSingle(condition, ctx));
        }

        @Test
        void shouldNotMatchLessNumeric() {
            Condition condition = buildCondition("query.age", ConditionOperator.GT, "18");
            EvaluationContext ctx = contextWith(Map.of(), Map.of("age", "10"), Map.of());
            assertFalse(evaluator.evaluateSingle(condition, ctx));
        }

        @Test
        void shouldReturnFalseForNullActual() {
            Condition condition = buildCondition("query.age", ConditionOperator.GT, "18");
            EvaluationContext ctx = contextWith(Map.of(), Map.of(), Map.of());
            assertFalse(evaluator.evaluateSingle(condition, ctx));
        }
    }

    @Nested
    @DisplayName("LT operator")
    class LtOperator {

        @Test
        void shouldMatchLessNumeric() {
            Condition condition = buildCondition("query.count", ConditionOperator.LT, "100");
            EvaluationContext ctx = contextWith(Map.of(), Map.of("count", "50"), Map.of());
            assertTrue(evaluator.evaluateSingle(condition, ctx));
        }

        @Test
        void shouldNotMatchGreaterNumeric() {
            Condition condition = buildCondition("query.count", ConditionOperator.LT, "100");
            EvaluationContext ctx = contextWith(Map.of(), Map.of("count", "200"), Map.of());
            assertFalse(evaluator.evaluateSingle(condition, ctx));
        }
    }

    @Nested
    @DisplayName("CONTAINS operator")
    class ContainsOp {

        @Test
        void shouldMatchSubstring() {
            Condition condition = buildCondition("header.user-agent", ConditionOperator.CONTAINS, "Chrome");
            EvaluationContext ctx = contextWith(Map.of("user-agent", "Mozilla/5.0 Chrome/100"), Map.of(), Map.of());
            assertTrue(evaluator.evaluateSingle(condition, ctx));
        }

        @Test
        void shouldNotMatchMissingSubstring() {
            Condition condition = buildCondition("header.user-agent", ConditionOperator.CONTAINS, "Firefox");
            EvaluationContext ctx = contextWith(Map.of("user-agent", "Mozilla/5.0 Chrome/100"), Map.of(), Map.of());
            assertFalse(evaluator.evaluateSingle(condition, ctx));
        }
    }

    @Nested
    @DisplayName("IN operator")
    class InOp {

        @Test
        void shouldMatchValueInList() {
            Condition condition = buildCondition("query.status", ConditionOperator.IN, "active,pending,review");
            EvaluationContext ctx = contextWith(Map.of(), Map.of("status", "pending"), Map.of());
            assertTrue(evaluator.evaluateSingle(condition, ctx));
        }

        @Test
        void shouldNotMatchValueNotInList() {
            Condition condition = buildCondition("query.status", ConditionOperator.IN, "active,pending,review");
            EvaluationContext ctx = contextWith(Map.of(), Map.of("status", "deleted"), Map.of());
            assertFalse(evaluator.evaluateSingle(condition, ctx));
        }
    }

    @Nested
    @DisplayName("REGEX operator")
    class RegexOp {

        @Test
        void shouldMatchRegex() {
            Condition condition = buildCondition("path.id", ConditionOperator.REGEX, "\\d+");
            EvaluationContext ctx = contextWith(Map.of(), Map.of(), Map.of("id", "12345"));
            assertTrue(evaluator.evaluateSingle(condition, ctx));
        }

        @Test
        void shouldNotMatchBadRegexInput() {
            Condition condition = buildCondition("path.id", ConditionOperator.REGEX, "\\d+");
            EvaluationContext ctx = contextWith(Map.of(), Map.of(), Map.of("id", "abc"));
            assertFalse(evaluator.evaluateSingle(condition, ctx));
        }

        @Test
        void shouldHandleInvalidRegexGracefully() {
            Condition condition = buildCondition("path.id", ConditionOperator.REGEX, "[invalid");
            EvaluationContext ctx = contextWith(Map.of(), Map.of(), Map.of("id", "test"));
            assertFalse(evaluator.evaluateSingle(condition, ctx));
        }
    }

    @Nested
    @DisplayName("STARTS_WITH / ENDS_WITH operators")
    class PrefixSuffix {

        @Test
        void shouldMatchStartsWith() {
            Condition condition = buildCondition("header.content-type", ConditionOperator.STARTS_WITH, "application/");
            EvaluationContext ctx = contextWith(Map.of("content-type", "application/json"), Map.of(), Map.of());
            assertTrue(evaluator.evaluateSingle(condition, ctx));
        }

        @Test
        void shouldMatchEndsWith() {
            Condition condition = buildCondition("header.content-type", ConditionOperator.ENDS_WITH, "/json");
            EvaluationContext ctx = contextWith(Map.of("content-type", "application/json"), Map.of(), Map.of());
            assertTrue(evaluator.evaluateSingle(condition, ctx));
        }
    }

    @Nested
    @DisplayName("AND logic (multiple conditions)")
    class AndLogic {

        @Test
        void shouldReturnTrueWhenAllConditionsMatch() {
            List<Condition> conditions = List.of(
                    buildCondition("header.x-api-key", ConditionOperator.EQ, "secret"),
                    buildCondition("query.limit", ConditionOperator.GT, "5"),
                    buildCondition("path.id", ConditionOperator.REGEX, "\\d+"));
            EvaluationContext ctx = contextWith(
                    Map.of("x-api-key", "secret"),
                    Map.of("limit", "10"),
                    Map.of("id", "42"));
            assertTrue(evaluator.evaluate(conditions, ctx));
        }

        @Test
        void shouldReturnFalseWhenOneConditionFails() {
            List<Condition> conditions = List.of(
                    buildCondition("header.x-api-key", ConditionOperator.EQ, "secret"),
                    buildCondition("query.limit", ConditionOperator.GT, "100"));
            EvaluationContext ctx = contextWith(
                    Map.of("x-api-key", "secret"),
                    Map.of("limit", "10"),
                    Map.of());
            assertFalse(evaluator.evaluate(conditions, ctx));
        }
    }

    @Nested
    @DisplayName("Null/missing value handling")
    class NullHandling {

        @Test
        void shouldHandleNullActualForEq() {
            Condition condition = buildCondition("header.missing", ConditionOperator.EQ, "value");
            EvaluationContext ctx = contextWith(Map.of(), Map.of(), Map.of());
            assertFalse(evaluator.evaluateSingle(condition, ctx));
        }

        @Test
        void shouldHandleNullActualForNeq() {
            Condition condition = buildCondition("header.missing", ConditionOperator.NEQ, "value");
            EvaluationContext ctx = contextWith(Map.of(), Map.of(), Map.of());
            assertTrue(evaluator.evaluateSingle(condition, ctx));
        }

        @Test
        void shouldHandleNullActualForContains() {
            Condition condition = buildCondition("header.missing", ConditionOperator.CONTAINS, "val");
            EvaluationContext ctx = contextWith(Map.of(), Map.of(), Map.of());
            assertFalse(evaluator.evaluateSingle(condition, ctx));
        }

        @Test
        void shouldHandleNullActualForIn() {
            Condition condition = buildCondition("header.missing", ConditionOperator.IN, "a,b,c");
            EvaluationContext ctx = contextWith(Map.of(), Map.of(), Map.of());
            assertFalse(evaluator.evaluateSingle(condition, ctx));
        }

        @Test
        void shouldHandleInvalidSourcePrefix() {
            Condition condition = buildCondition("body.field", ConditionOperator.EQ, "value");
            EvaluationContext ctx = contextWith(Map.of(), Map.of(), Map.of());
            assertFalse(evaluator.evaluateSingle(condition, ctx));
        }

        @Test
        void shouldHandleMalformedKey() {
            Condition condition = buildCondition("noDotKey", ConditionOperator.EQ, "value");
            EvaluationContext ctx = contextWith(Map.of(), Map.of(), Map.of());
            assertFalse(evaluator.evaluateSingle(condition, ctx));
        }
    }
}

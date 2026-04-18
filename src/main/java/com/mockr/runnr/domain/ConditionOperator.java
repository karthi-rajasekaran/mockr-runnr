package com.mockr.runnr.domain;

/**
 * ConditionOperator Enum - Represents all supported operators for conditional
 * matching.
 * 
 * Used in rule conditions to evaluate:
 * - Headers (e.g., header.x-api-key EQ abc)
 * - Query parameters (e.g., query.order NEQ 10)
 * - Path variables (e.g., path.id GT 100)
 * - Body content (e.g., body CONTAINS token)
 */
public enum ConditionOperator {

    /**
     * Equality operator
     */
    EQ("EQ", "Equals"),

    /**
     * Not equals operator
     */
    NEQ("NEQ", "Not equals"),

    /**
     * Greater than operator
     */
    GT("GT", "Greater than"),

    /**
     * Less than operator
     */
    LT("LT", "Less than"),

    /**
     * Greater than or equals operator
     */
    GTE("GTE", "Greater than or equals"),

    /**
     * Less than or equals operator
     */
    LTE("LTE", "Less than or equals"),

    /**
     * Contains substring operator
     */
    CONTAINS("CONTAINS", "Contains"),

    /**
     * Does not contain operator
     */
    NOT_CONTAINS("NOT_CONTAINS", "Does not contain"),

    /**
     * In list operator
     */
    IN("IN", "In list"),

    /**
     * Not in list operator
     */
    NOT_IN("NOT_IN", "Not in list"),

    /**
     * Regex match operator
     */
    REGEX("REGEX", "Regex match"),

    /**
     * Starts with operator
     */
    STARTS_WITH("STARTS_WITH", "Starts with"),

    /**
     * Ends with operator
     */
    ENDS_WITH("ENDS_WITH", "Ends with");

    private final String code;
    private final String description;

    ConditionOperator(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Parse operator from string code.
     * Case-insensitive to support various input formats.
     */
    public static ConditionOperator fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Operator code cannot be null or empty");
        }

        for (ConditionOperator operator : ConditionOperator.values()) {
            if (operator.code.equalsIgnoreCase(code)) {
                return operator;
            }
        }

        throw new IllegalArgumentException("Unknown operator: " + code);
    }
}

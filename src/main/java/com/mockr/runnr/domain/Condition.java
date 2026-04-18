package com.mockr.runnr.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Condition Entity - Represents a rule/condition for conditional response
 * matching.
 * 
 * Structure optimized for rule engine integration:
 * - lhs: Left-hand side (header.x-api-key, query.order, path.id, etc.)
 * - operation: Operator enum (EQ, NEQ, GT, LT, CONTAINS, REGEX, etc.)
 * - rhs: Right-hand side (the value to compare against)
 * 
 * The operation field now uses the ConditionOperator enum for type safety
 * instead of a plain String, enabling better validation and IDE support.
 * 
 * Future: Can be extended with:
 * - operationType enum (HEADER, QUERY, PATH, BODY_JSON, BODY_XML)
 * - For rule engine: Transform to Rule object with Predicate<Request>
 */
@Entity
@Table(name = "condition", indexes = {
        @Index(name = "idx_response_id", columnList = "response_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "response")
@EqualsAndHashCode(exclude = "response")
public class Condition {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinColumn(name = "response_id", nullable = false)
    private Response response;

    @Column(nullable = false, length = 100)
    private String lhs; // left-hand side (e.g., "header.x-api-key")

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConditionOperator operation; // operator enum (EQ, NEQ, GT, LT, CONTAINS, IN, REGEX, etc.)

    @Column(columnDefinition = "TEXT")
    private String rhs; // right-hand side (the value to match)

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

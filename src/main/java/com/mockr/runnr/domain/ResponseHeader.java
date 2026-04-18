package com.mockr.runnr.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * ResponseHeader Entity - Represents HTTP response headers to be returned with
 * the mock response.
 * 
 * Optimization notes:
 * - LAZY loading with bidirectional mapping
 * - CASCADE.PERSIST/MERGE for nested saves
 */
@Entity
@Table(name = "response_header", indexes = {
        @Index(name = "idx_response_id_hdr", columnList = "response_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "response")
@EqualsAndHashCode(exclude = "response")
public class ResponseHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinColumn(name = "response_id", nullable = false)
    private Response response;

    @Column(nullable = false, length = 200)
    private String headerKey;

    @Column(columnDefinition = "TEXT")
    private String headerValue;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

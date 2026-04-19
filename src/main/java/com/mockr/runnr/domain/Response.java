package com.mockr.runnr.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Response Entity - Represents a mock response with conditions and headers.
 * 
 * Optimization notes:
 * - Bidirectional relationship with Endpoint (use mappedBy to avoid duplicate
 * FK)
 * - CASCADE.PERSIST/MERGE allows nested saves
 * - orphanRemoval = true ensures cleanup
 * - LAZY loading prevents unnecessary data fetching
 * - Collections are Set<> not List<> to allow multiple JOIN FETCH (fixes
 * MultipleBagFetchException)
 */
@Entity
@Table(name = "response", indexes = {
                @Index(name = "idx_endpoint_id", columnList = "endpoint_id"),
                @Index(name = "idx_status_code", columnList = "status_code")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = { "endpoint", "conditions", "responseHeaders" })
@EqualsAndHashCode(exclude = { "endpoint", "conditions", "responseHeaders" })
public class Response {

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        @Column(columnDefinition = "TEXT")
        private UUID id;

        @ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
        @JoinColumn(name = "endpoint_id", nullable = false)
        private Endpoint endpoint;

        @Column(name = "status_code", nullable = false)
        private Integer statusCode;

        @Column(name = "content_type", length = 100)
        @Builder.Default
        private String contentType = "application/json";

        @Column(name = "response_body", columnDefinition = "TEXT")
        private String responseBody;

        @Column(name = "is_default", columnDefinition = "INTEGER")
        @Builder.Default
        private Boolean isDefault = false;

        @Column(name = "description", length = 500)
        private String description;

        @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE,
                        CascadeType.REMOVE }, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "response")
        @Builder.Default
        private Set<Condition> conditions = new HashSet<>();

        @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE,
                        CascadeType.REMOVE }, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "response")
        @Builder.Default
        private Set<ResponseHeader> responseHeaders = new HashSet<>();

        @Column(name = "created_at", nullable = false, updatable = false)
        @Builder.Default
        private LocalDateTime createdAt = LocalDateTime.now();

        @Column(name = "updated_at")
        private LocalDateTime updatedAt;

        @PreUpdate
        private void onUpdate() {
                this.updatedAt = LocalDateTime.now();
        }
}

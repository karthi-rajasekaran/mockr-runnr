package com.mockr.runnr.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Endpoint Entity - Represents a mock API endpoint.
 * 
 * Optimization notes:
 * - LAZY loading on project reference (avoid N+1 queries)
 * - CASCADE.PERSIST allows saving responses in one transaction
 * - orphanRemoval = true ensures deleted responses are cleaned from DB
 * - EAGER loading is NOT used to prevent unnecessary data fetching
 * - Collections are Set<> not List<> to allow multiple JOIN FETCH (fixes
 * MultipleBagFetchException)
 */
@Entity
@Table(name = "endpoint", indexes = {
        @Index(name = "idx_project_id", columnList = "project_id"),
        @Index(name = "idx_path_method", columnList = "path,method")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "responses") // Avoid lazy loading issues in toString
@EqualsAndHashCode(exclude = "responses") // Exclude collections from equals/hashCode
public class Endpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "TEXT")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 500)
    private String path;

    @Column(nullable = false, length = 10)
    private String method;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE,
            CascadeType.REMOVE }, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "endpoint")
    @Builder.Default
    private Set<Response> responses = new HashSet<>();

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

package com.mockr.runnr.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mockr.runnr.domain.Endpoint;

/**
 * EndpointRepository - Read-only data access for Endpoint entity.
 * 
 * Optimized for Mockr Runnr's runtime matching needs:
 * - @EntityGraph eliminates N+1 queries on nested responses, conditions, and
 * headers
 * - Single optimized query per method call
 * - SQLite-friendly approach avoiding complex FETCH JOIN cartesian products
 * 
 * All methods are read-only to prevent accidental mutations.
 */
@Repository
@Transactional(readOnly = true)
public interface EndpointRepository extends JpaRepository<Endpoint, UUID> {

        /**
         * Find all endpoints for a project with all nested data.
         * Used by CacheService for caching all endpoints at startup.
         * 
         * EntityGraph loads: responses, conditions, responseHeaders in optimized
         * manner.
         * 
         * @param projectId Project UUID
         * @return List of endpoints with full response tree
         */
        @EntityGraph(attributePaths = { "responses", "responses.conditions", "responses.responseHeaders" })
        List<Endpoint> findAllByProjectId(UUID projectId);

        /**
         * Find endpoint by ID with all nested data.
         * Used by ResponseResolver to load endpoint details.
         * 
         * @param id Endpoint UUID
         * @return Optional endpoint with full response tree
         */
        @EntityGraph(attributePaths = { "responses", "responses.conditions", "responses.responseHeaders" })
        Optional<Endpoint> findById(UUID id);

        /**
         * Find endpoint by path, method, and project ID with all nested data.
         * This is the CRITICAL method for direct endpoint lookup by request routing.
         * 
         * EntityGraph loads: responses, conditions, responseHeaders in single query.
         * 
         * @param path      Endpoint path (e.g., "/api/payment")
         * @param method    HTTP method (GET, POST, etc.)
         * @param projectId Project UUID
         * @return Optional endpoint if found
         */
        @EntityGraph(attributePaths = { "responses", "responses.conditions", "responses.responseHeaders" })
        Optional<Endpoint> findByPathAndMethodAndProjectId(String path, String method, UUID projectId);

        /**
         * Check endpoint existence without loading full object (lightweight check).
         * Used to validate endpoint exists before processing.
         * 
         * @param path      Endpoint path
         * @param method    HTTP method
         * @param projectId Project UUID
         * @return true if endpoint exists, false otherwise
         */
        @Query("SELECT COUNT(e) > 0 FROM Endpoint e " +
                        "WHERE e.path = :path " +
                        "AND e.method = :method " +
                        "AND e.project.id = :projectId")
        boolean existsByPathAndMethodAndProjectId(
                        @Param("path") String path,
                        @Param("method") String method,
                        @Param("projectId") UUID projectId);
}

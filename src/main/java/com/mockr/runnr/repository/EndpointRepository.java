package com.mockr.runnr.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
 * - FETCH JOIN eliminates N+1 queries on nested responses
 * - LEFT JOIN FETCH for optional collections
 * - findByProjectIdAndMethodAndPath: Critical for request routing
 * 
 * All methods are read-only to prevent accidental mutations.
 */
@Repository
@Transactional(readOnly = true)
public interface EndpointRepository extends JpaRepository<Endpoint, UUID> {

    /**
     * Find endpoint by path, method, and project ID with all nested data.
     * This is the CRITICAL method for Mockr Runnr's request routing.
     * 
     * Fetches the entire response tree in a single query to avoid N+1 problems.
     */
    @Query("SELECT DISTINCT e FROM Endpoint e " +
            "LEFT JOIN FETCH e.responses r " +
            "LEFT JOIN FETCH r.conditions " +
            "LEFT JOIN FETCH r.responseHeaders " +
            "WHERE e.project.id = :projectId " +
            "AND e.method = :method " +
            "AND e.path = :path")
    Optional<Endpoint> findByProjectIdAndMethodAndPath(
            @Param("projectId") UUID projectId,
            @Param("method") String method,
            @Param("path") String path);

    /**
     * Find endpoint by ID with all nested data loaded (eliminates N+1 queries).
     */
    @Query("SELECT DISTINCT e FROM Endpoint e " +
            "LEFT JOIN FETCH e.responses r " +
            "LEFT JOIN FETCH r.conditions " +
            "LEFT JOIN FETCH r.responseHeaders " +
            "WHERE e.id = :id")
    Optional<Endpoint> findByIdWithDetails(@Param("id") UUID id);

    /**
     * Find all endpoints for a project with all nested data.
     * Used for caching or bulk operations.
     */
    @Query("SELECT DISTINCT e FROM Endpoint e " +
            "LEFT JOIN FETCH e.responses r " +
            "LEFT JOIN FETCH r.conditions " +
            "LEFT JOIN FETCH r.responseHeaders " +
            "WHERE e.project.id = :projectId")
    List<Endpoint> findAllByProjectIdWithDetails(@Param("projectId") UUID projectId);

    /**
     * Check endpoint existence without loading full object (lightweight check).
     */
    @Query("SELECT COUNT(e) > 0 FROM Endpoint e " +
            "WHERE e.path = :path " +
            "AND e.method = :method " +
            "AND e.project.id = :projectId")
    boolean existsByPathAndMethodAndProjectId(
            @Param("path") String path,
            @Param("method") String method,
            @Param("projectId") UUID projectId);

    /**
     * Find endpoint by path and method for a project (simple lookup).
     */
    Optional<Endpoint> findByPathAndMethodAndProjectId(String path, String method, UUID projectId);

    /**
     * Find all endpoints for a project (read-only).
     */
    List<Endpoint> findAllByProjectId(UUID projectId);
}

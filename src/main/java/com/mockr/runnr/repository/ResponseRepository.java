package com.mockr.runnr.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mockr.runnr.domain.Response;

/**
 * ResponseRepository - Read-only data access for Response entity.
 * 
 * Optimized for Mockr Runnr's response lookup needs:
 * - FETCH JOIN for nested conditions and headers
 * - No mutation methods (delete, save, update) - read-only only
 */
@Repository
@Transactional(readOnly = true)
public interface ResponseRepository extends JpaRepository<Response, UUID> {

    /**
     * Find all responses for an endpoint with all nested data.
     * Loads conditions and headers in a single query to avoid N+1 problems.
     */
    @Query("SELECT DISTINCT r FROM Response r " +
            "LEFT JOIN FETCH r.conditions " +
            "LEFT JOIN FETCH r.responseHeaders " +
            "WHERE r.endpoint.id = :endpointId")
    List<Response> findAllByEndpointIdWithDetails(@Param("endpointId") UUID endpointId);

    /**
     * Find all responses for an endpoint (simple lookup).
     */
    List<Response> findAllByEndpointId(UUID endpointId);

    /**
     * Find responses by project ID (for bulk operations).
     */
    @Query("SELECT DISTINCT r FROM Response r " +
            "LEFT JOIN FETCH r.conditions " +
            "LEFT JOIN FETCH r.responseHeaders " +
            "WHERE r.endpoint.project.id = :projectId")
    List<Response> findAllByProjectId(@Param("projectId") UUID projectId);
}

package com.mockr.runnr.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mockr.runnr.domain.ResponseHeader;

/**
 * ResponseHeaderRepository - Read-only data access for ResponseHeader entity.
 * 
 * Optimized for Mockr Runnr's response header lookup:
 * - findByResponseId: Fetch all headers for a response
 * - No mutation methods - read-only only
 */
@Repository
@Transactional(readOnly = true)
public interface ResponseHeaderRepository extends JpaRepository<ResponseHeader, UUID> {

    /**
     * Find all headers for a response.
     * Used to populate HTTP response headers in the mock response.
     */
    List<ResponseHeader> findByResponseId(UUID responseId);

    /**
     * Find all headers for a response by explicit query (optimized).
     */
    @Query("SELECT h FROM ResponseHeader h WHERE h.response.id = :responseId ORDER BY h.createdAt ASC")
    List<ResponseHeader> findAllByResponseIdOrdered(@Param("responseId") UUID responseId);
}

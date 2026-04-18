package com.mockr.runnr.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mockr.runnr.domain.Condition;

/**
 * ConditionRepository - Read-only data access for Condition entity.
 * 
 * Optimized for Mockr Runnr's condition evaluation needs:
 * - findByResponseId: Fetch all conditions for a response to evaluate rules
 * - No mutation methods - read-only only
 * 
 * Future enhancement:
 * - Query conditions by operator for rule engine optimization
 * - Fetch conditions by LHS (left-hand side) for header/query/path filtering
 */
@Repository
@Transactional(readOnly = true)
public interface ConditionRepository extends JpaRepository<Condition, UUID> {

    /**
     * Find all conditions for a response.
     * Critical for evaluating conditional responses.
     */
    List<Condition> findByResponseId(UUID responseId);

    /**
     * Find all conditions for a response by explicit query (optimized).
     */
    @Query("SELECT c FROM Condition c WHERE c.response.id = :responseId ORDER BY c.createdAt ASC")
    List<Condition> findAllByResponseIdOrdered(@Param("responseId") UUID responseId);
}

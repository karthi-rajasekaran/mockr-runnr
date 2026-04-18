package com.mockr.runnr.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mockr.runnr.domain.Project;

/**
 * ProjectRepository - Read-only data access for Project entity.
 * Optimized for runtime lookup with minimal queries.
 */
@Repository
@Transactional(readOnly = true)
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    /**
     * Check if project exists by name (for validation).
     */
    @Query("SELECT COUNT(p) > 0 FROM Project p WHERE p.name = :name")
    boolean existsByName(@Param("name") String name);
}

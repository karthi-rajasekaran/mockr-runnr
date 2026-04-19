package com.mockr.runnr.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.mockr.runnr.domain.Endpoint;
import com.mockr.runnr.domain.Project;
import com.mockr.runnr.repository.EndpointRepository;
import com.mockr.runnr.repository.ProjectRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * CacheService - On-demand caching service for endpoint and project
 * configurations.
 * 
 * Leverages Caffeine cache with 10-minute idle expiry for production-grade
 * caching.
 * 
 * Features:
 * - @Cacheable for automatic on-demand loading
 * - Fallback to repository on cache miss
 * - expireAfterAccess(10 minutes) for TTL management
 * - Cache statistics for monitoring (hits, misses, evictions)
 * - Thread-safe operations (built-in to Caffeine)
 * - Optimal performance (compiled bytecode, minimal reflection)
 * 
 * Cache names:
 * - "endpoints": List of endpoints by project ID
 * - "endpoint": Single endpoint by ID
 * - "project": Project details by context path
 */
@Service
@Slf4j
public class CacheService {

    private final EndpointRepository endpointRepository;
    private final ProjectRepository projectRepository;
    private final CacheManager cacheManager;

    public CacheService(EndpointRepository endpointRepository,
            ProjectRepository projectRepository,
            CacheManager cacheManager) {
        this.endpointRepository = endpointRepository;
        this.projectRepository = projectRepository;
        this.cacheManager = cacheManager;
    }

    /**
     * Get or load all endpoints for a project from cache.
     * 
     * Cache name: "endpoints"
     * Cache key: projectId (UUID)
     * TTL: 10 minutes of inactivity (expireAfterAccess)
     * 
     * On first access or cache miss:
     * - Loads all endpoints from repository
     * - Includes responses, conditions, and headers (FETCH JOIN)
     * - Stores in cache for subsequent accesses
     * 
     * On cache hit:
     * - Returns cached list immediately
     * - Resets 10-minute timer
     * 
     * @param projectId Project UUID to load endpoints for
     * @return Cached or freshly loaded list of endpoints
     */
    @Cacheable(value = "endpoints", key = "#projectId", unless = "#result == null || #result.isEmpty()")
    public List<Endpoint> getOrLoadEndpoints(UUID projectId) {
        log.debug("Cache miss for endpoints, loading from repository: projectId={}", projectId);
        List<Endpoint> endpoints = endpointRepository.findAllByProjectIdWithDetails(projectId);
        log.debug("Loaded {} endpoints from repository: projectId={}", endpoints.size(), projectId);
        return endpoints;
    }

    /**
     * Get or load a single endpoint by ID from cache.
     * 
     * Cache name: "endpoint"
     * Cache key: endpointId (UUID)
     * TTL: 10 minutes of inactivity (expireAfterAccess)
     * 
     * On first access or cache miss:
     * - Loads endpoint from repository
     * - Includes responses, conditions, and headers (FETCH JOIN)
     * - Stores in cache for subsequent accesses
     * 
     * On cache hit:
     * - Returns cached endpoint immediately
     * - Resets 10-minute timer
     * 
     * @param endpointId Endpoint UUID to load
     * @return Cached or freshly loaded endpoint wrapped in Optional
     */
    @Cacheable(value = "endpoint", key = "#endpointId", unless = "#result == null")
    public Optional<Endpoint> getOrLoadEndpoint(UUID endpointId) {
        log.debug("Cache miss for endpoint, loading from repository: endpointId={}", endpointId);
        Optional<Endpoint> endpoint = endpointRepository.findByIdWithDetails(endpointId);
        if (endpoint.isPresent()) {
            log.debug("Loaded endpoint from repository: endpointId={}", endpointId);
        } else {
            log.debug("Endpoint not found in repository: endpointId={}", endpointId);
        }
        return endpoint;
    }

    /**
     * Get or load a project by context path from cache.
     * 
     * Cache name: "project"
     * Cache key: contextPath (String)
     * TTL: 10 minutes of inactivity (expireAfterAccess)
     * 
     * Context path is normalized before lookup (leading/trailing slashes removed).
     * 
     * On first access or cache miss:
     * - Loads project from repository using normalized context path
     * - Stores in cache for subsequent accesses
     * 
     * On cache hit:
     * - Returns cached project immediately
     * - Resets 10-minute timer
     * 
     * @param contextPath The context path to search for (e.g., "mockr", "/mockr/")
     * @return Cached or freshly loaded project wrapped in Optional
     */
    @Cacheable(value = "project", key = "#contextPath", unless = "#result == null")
    public Optional<Project> getOrLoadProjectByContextPath(String contextPath) {
        log.debug("Cache miss for project, loading from repository: contextPath={}", contextPath);
        Optional<Project> project = projectRepository.findByContextPath(contextPath);
        if (project.isPresent()) {
            log.debug("Loaded project from repository: contextPath={}, projectId={}", contextPath,
                    project.get().getId());
        } else {
            log.debug("Project not found in repository: contextPath={}", contextPath);
        }
        return project;
    }

    /**
     * Invalidate all cached data (endpoints, endpoint, and project caches).
     * 
     * Use when:
     * - Configuration is updated in database
     * - Manual cache flush is needed
     * - Cache corruption is suspected
     * 
     * After eviction:
     * - Next access will trigger cache miss
     * - Fresh data will be loaded from repository
     * - New 10-minute timer starts
     */
    @CacheEvict(allEntries = true, cacheNames = { "endpoints", "endpoint", "project" })
    public void invalidateAllCaches() {
        log.info("Cache invalidation requested - all cached data cleared");
    }

    /**
     * Get cache statistics for the "endpoints" cache.
     * 
     * Exposes Caffeine's built-in statistics:
     * - hits: Number of cache hits
     * - misses: Number of cache misses
     * - evictions: Number of entries evicted due to expiration or size limits
     * - loadSuccesses: Number of successful cache loads
     * - loadFailures: Number of failed cache loads
     * - totalLoadTime: Total time spent loading cache entries
     * 
     * Use for monitoring cache effectiveness and performance tuning.
     * 
     * @return Map of cache statistics
     */
    public Map<String, Object> getEndpointsCacheStats() {
        return extractCacheStats("endpoints");
    }

    /**
     * Get cache statistics for the "endpoint" cache.
     * 
     * @return Map of cache statistics
     */
    public Map<String, Object> getEndpointCacheStats() {
        return extractCacheStats("endpoint");
    }

    /**
     * Extract Caffeine cache statistics from a named cache.
     * 
     * @param cacheName Name of the cache ("endpoints" or "endpoint")
     * @return Map with cache statistics or empty map if unavailable
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> extractCacheStats(String cacheName) {
        Map<String, Object> stats = new HashMap<>();

        var cache = cacheManager.getCache(cacheName);
        if (cache != null && cache.getNativeCache() instanceof Cache) {
            Cache<Object, Object> caffeineCache = (Cache<Object, Object>) cache
                    .getNativeCache();

            var cacheStats = caffeineCache.stats();
            stats.put("cacheName", cacheName);
            stats.put("hitCount", cacheStats.hitCount());
            stats.put("missCount", cacheStats.missCount());
            stats.put("evictionCount", cacheStats.evictionCount());
            stats.put("loadSuccessCount", cacheStats.loadSuccessCount());
            stats.put("loadFailureCount", cacheStats.loadFailureCount());
            stats.put("totalLoadTime", cacheStats.totalLoadTime());
            stats.put("hitRate", cacheStats.hitRate());
            stats.put("missRate", cacheStats.missRate());
        }

        return stats;
    }

    /**
     * Get combined cache statistics for all caches.
     * 
     * @return Map containing statistics for "endpoints", "endpoint", and "project"
     *         caches
     */
    public Map<String, Object> getAllCacheStats() {
        Map<String, Object> allStats = new HashMap<>();
        allStats.put("endpoints", getEndpointsCacheStats());
        allStats.put("endpoint", getEndpointCacheStats());
        allStats.put("project", getProjectCacheStats());
        return allStats;
    }

    /**
     * Get cache statistics for the "project" cache.
     * 
     * @return Map of cache statistics
     */
    public Map<String, Object> getProjectCacheStats() {
        return extractCacheStats("project");
    }
}

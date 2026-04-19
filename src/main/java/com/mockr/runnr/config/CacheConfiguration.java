package com.mockr.runnr.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * CacheConfiguration - Spring configuration for Caffeine cache with Mockr Runnr
 * settings.
 * 
 * Configures:
 * - Expiration policy: 10 minutes of idle time (expireAfterAccess)
 * - Cache statistics: enabled for monitoring
 * - Thread-safety: built-in to Caffeine
 * - Performance: compiled code, minimal reflection
 * 
 * Cache names configured: "endpoints", "endpoint", "project"
 */
@Configuration
@EnableCaching
public class CacheConfiguration {

    /**
     * Configure Caffeine cache with 10-minute idle expiry and statistics.
     * 
     * @return configured Caffeine instance
     */
    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
        return Caffeine.newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .recordStats();
    }

    /**
     * Create Spring CacheManager with Caffeine backend.
     * 
     * Configures named caches for:
     * - "endpoints": cached list of endpoints by project ID
     * - "endpoint": cached single endpoint by ID
     * - "project": cached project details by context path
     * 
     * @param caffeineConfig Caffeine configuration
     * @return CacheManager with Caffeine backend
     */
    @Bean
    public CacheManager cacheManager(Caffeine<Object, Object> caffeineConfig) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("endpoints", "endpoint", "project");
        cacheManager.setCaffeine(caffeineConfig);
        return cacheManager;
    }
}

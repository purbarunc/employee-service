package com.purbarun.employee.config.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine cache configuration for L1 (fast memory) cache.
 * Provides high-performance in-memory caching with 5-minute TTL.
 */
@Configuration
@EnableCaching
public class CaffeineCacheConfig {

    @Bean(name = "caffeineCacheManager")
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES) // 5 minutes TTL
                .maximumSize(500) // Maximum 500 entries
                .recordStats()); // Enable statistics
        
        // Pre-configure cache names
        cacheManager.setCacheNames(java.util.List.of(
            "l1Employees",
            "l1EmployeeById", 
            "l1EmployeesByDepartment",
            "l1EmployeeByEmail",
            "l1SearchResults",
            "l1Addresses",
            "l1Statistics"
        ));
        
        return cacheManager;
    }
}

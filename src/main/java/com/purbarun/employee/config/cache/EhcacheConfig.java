package com.purbarun.employee.config.cache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Ehcache configuration for L2 (persistent) cache.
 * Provides persistent caching to store entire database in cache memory.
 */
@Configuration
@EnableCaching
public class EhcacheConfig {

    @Bean(name = "ehcacheManager")
    CacheManager ehcacheManager() {
        try {
            CachingProvider provider = Caching.getCachingProvider();
            URI configUri = getClass().getResource("/ehcache.xml").toURI();
            javax.cache.CacheManager jCacheManager = provider.getCacheManager(
                configUri,
                getClass().getClassLoader()
            );
            
            return new org.springframework.cache.jcache.JCacheCacheManager(jCacheManager);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to load ehcache.xml configuration", e);
        }
    }
}

package com.purbarun.employee.config.cache;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.purbarun.employee.service.cache.TwoLevelCacheManagerImpl;

/**
 * Configuration for two-level caching system. L1: Caffeine (fast memory,
 * 5-minute TTL) L2: Ehcache (persistent, entire database cache)
 */
@Configuration
@EnableCaching
@EnableScheduling
public class TwoLevelCacheConfig {
	@Bean(name = "twoLevelCacheManager")
	@Primary
	TwoLevelCacheManagerImpl twoLevelCacheManager(@Qualifier("caffeineCacheManager") CacheManager l1CacheManager,
			@Qualifier("ehcacheManager") CacheManager l2CacheManager) {
		return new TwoLevelCacheManagerImpl(l1CacheManager, l2CacheManager);
	}
}

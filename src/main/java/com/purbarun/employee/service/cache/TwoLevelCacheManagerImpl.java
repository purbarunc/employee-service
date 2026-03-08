package com.purbarun.employee.service.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import com.purbarun.employee.dto.cache.CacheStatistics;

import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Two-level cache manager following the dummy-service pattern. Manages L1 (fast
 * memory) and L2 (persistent) caches with statistics.
 */
public class TwoLevelCacheManagerImpl implements CacheManager {

	private static final Logger logger = LoggerFactory.getLogger(TwoLevelCacheManagerImpl.class);

	private final CacheManager l1CacheManager;
	private final CacheManager l2CacheManager;

	// Cache statistics
	private final Map<String, AtomicLong> l1HitCount = new ConcurrentHashMap<>();
	private final Map<String, AtomicLong> l1MissCount = new ConcurrentHashMap<>();
	private final Map<String, AtomicLong> l2HitCount = new ConcurrentHashMap<>();
	private final Map<String, AtomicLong> l2MissCount = new ConcurrentHashMap<>();

	public TwoLevelCacheManagerImpl(@Qualifier("caffeineCacheManager") CacheManager l1CacheManager,
			@Qualifier("ehcacheManager") CacheManager l2CacheManager) {
		this.l1CacheManager = l1CacheManager;
		this.l2CacheManager = l2CacheManager;
	}

	@Override
	public Cache getCache(String name) {
		Cache l1Cache = null;
		Cache l2Cache = null;

		// Only try to get L1 cache if name starts with "l1"
		if (name.startsWith("l1")) {
			l1Cache = getL1Cache(name);
		}
		// Only try to get L2 cache if name starts with "l2"
		else if (name.startsWith("l2")) {
			l2Cache = getL2Cache(name);
		}
		// For other cache names, try both (for backward compatibility)
		else {
			l1Cache = getL1Cache(name);
			l2Cache = getL2Cache(name);
		}

		TwoLevelCacheImpl twoLevelCache = new TwoLevelCacheImpl(name, l1Cache, l2Cache);
		twoLevelCache.setCacheManager(this);
		return twoLevelCache;
	}

	private Cache getL1Cache(String name) {
		// If name already starts with "l1", use it directly
		String l1Name = name.startsWith("l1") ? name : "l1" + name.substring(0, 1).toUpperCase() + name.substring(1);
		Cache l1Cache = l1CacheManager.getCache(l1Name);
		if (l1Cache == null) {
			logger.warn("L1 Cache '{}' not found in cache manager", l1Name);
		} else {
			logger.debug("Successfully accessed L1 Cache '{}'", l1Name);
		}
		return l1Cache;
	}

	private Cache getL2Cache(String name) {
		// If name already starts with "l2", use it directly
		String l2Name = name.startsWith("l2") ? name : "l2" + name.substring(0, 1).toUpperCase() + name.substring(1);
		Cache l2Cache = l2CacheManager.getCache(l2Name);
		if (l2Cache == null) {
			logger.warn("L2 Cache '{}' not found in cache manager", l2Name);
		} else {
			logger.debug("Successfully accessed L2 Cache '{}'", l2Name);
		}
		return l2Cache;
	}

	@Override
	public java.util.Collection<String> getCacheNames() {
		return l1CacheManager.getCacheNames();
	}

	public CacheStatistics getStatistics(String cacheName) {
		return new CacheStatistics(l1HitCount.getOrDefault(cacheName, new AtomicLong(0)).get(),
				l1MissCount.getOrDefault(cacheName, new AtomicLong(0)).get(),
				l2HitCount.getOrDefault(cacheName, new AtomicLong(0)).get(),
				l2MissCount.getOrDefault(cacheName, new AtomicLong(0)).get());
	}

	public void recordL1Hit(String cacheName) {
		l1HitCount.computeIfAbsent(cacheName, _ -> new AtomicLong(0)).incrementAndGet();
	}

	public void recordL1Miss(String cacheName) {
		l1MissCount.computeIfAbsent(cacheName, _ -> new AtomicLong(0)).incrementAndGet();
	}

	public void recordL2Hit(String cacheName) {
		l2HitCount.computeIfAbsent(cacheName, _ -> new AtomicLong(0)).incrementAndGet();
	}

	public void recordL2Miss(String cacheName) {
		l2MissCount.computeIfAbsent(cacheName, _ -> new AtomicLong(0)).incrementAndGet();
	}

	public void refreshL1(String cacheName) {
		logger.info("Refreshing L1 cache: {}", cacheName);
		Cache l1Cache = l1CacheManager.getCache(cacheName);
		if (l1Cache != null) {
			l1Cache.clear();
			logger.info("Successfully cleared L1 cache: {}", cacheName);
		} else {
			logger.warn("L1 cache not found: {}", cacheName);
		}
	}

	public void refreshL2(String cacheName) {
		Cache l2Cache = l2CacheManager.getCache(cacheName);
		if (l2Cache != null) {
			l2Cache.clear();
		}
	}

	public void refreshAll() {
		// Clear all L1 caches
		l1CacheManager.getCacheNames().forEach(this::refreshL1);

		// Clear all L2 caches
		l2CacheManager.getCacheNames().forEach(this::refreshL2);
	}

	public String getAllStatistics() {
		StringBuilder stats = new StringBuilder();
		stats.append("=== Two-Level Cache Statistics ===\n");

		for (String cacheName : getCacheNames()) {
			CacheStatistics cacheStats = getStatistics(cacheName);
			stats.append(String.format("%s: %s\n", cacheName, cacheStats.toString()));
		}

		return stats.toString();
	}

}

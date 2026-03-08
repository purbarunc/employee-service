package com.purbarun.employee.service.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import java.util.concurrent.Callable;

/**
 * Two-level cache implementation using different cache technologies. L1 Cache:
 * Caffeine (fast memory, 5-minute TTL) L2 Cache: Ehcache (persistent, entire
 * database cache)
 */
public class TwoLevelCacheImpl implements Cache {

	private static final Logger logger = LoggerFactory.getLogger(TwoLevelCacheImpl.class);

	private final String name;
	private final Cache l1Cache;
	private final Cache l2Cache;
	private TwoLevelCacheManagerImpl cacheManager;

	public TwoLevelCacheImpl(String name, Cache l1Cache, Cache l2Cache) {
		this.name = name;
		this.l1Cache = l1Cache;
		this.l2Cache = l2Cache;
		this.cacheManager = null; // Will be injected
	}

	public void setCacheManager(TwoLevelCacheManagerImpl cacheManager) {
		this.cacheManager = cacheManager;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Object getNativeCache() {
		return l1Cache.getNativeCache();
	}

	@Override
	public ValueWrapper get(Object key) {
		logger.debug("CACHE[{}] - Searching key: {}", name, key);

		// Try L1 cache first (fast memory)
		long startTime = System.nanoTime();
		ValueWrapper value = l1Cache.get(key);
		long l1Time = System.nanoTime() - startTime;

		if (value != null) {
			logger.info("CACHE[{}] - L1 HIT for key: {} ({}ms)", name, key, l1Time / 1_000_000.0);
			if (cacheManager != null) {
				cacheManager.recordL1Hit(name);
			}
			return value;
		}

		logger.info("CACHE[{}] - L1 MISS for key: {} ({}ms)", name, key, l1Time / 1_000_000.0);

		// L1 miss, record it and try L2 cache
		if (cacheManager != null) {
			cacheManager.recordL1Miss(name);
		}

		startTime = System.nanoTime();
		value = l2Cache.get(key);
		long l2Time = System.nanoTime() - startTime;

		if (value != null) {
			logger.info("CACHE[{}] - L2 HIT for key: {} ({}ms) - PROMOTING to L1", name, key, l2Time / 1_000_000.0);
			if (cacheManager != null) {
				cacheManager.recordL2Hit(name);
			}
			// Promote to L1 cache for faster future access
			l1Cache.put(key, value.get());
			return value;
		}

		logger.warn("CACHE[{}] - L2 MISS for key: {} ({}ms) - DATABASE FETCH REQUIRED", name, key,
				l2Time / 1_000_000.0);

		// L2 miss as well
		if (cacheManager != null) {
			cacheManager.recordL2Miss(name);
		}

		return null;
	}

	@Override
	public <T> T get(Object key, Class<T> type) {
		ValueWrapper value = get(key);
		if (value != null) {
			Object cachedValue = value.get();
			if (cachedValue != null) {
				try {
					return type.cast(cachedValue);
				} catch (ClassCastException e) {
					return null; // Return null if type mismatch
				}
			}
		}
		return null;
	}

	/// Retrieves a value from the cache hierarchy, loading from the database if
	/// necessary.
	/// 
	/// This method implements the cache-aside pattern with a two-level hierarchy: 1.
	/// First checks L1 cache (fast memory) for immediate response 2. On L1 miss,
	/// checks L2 cache (persistent storage) 3. On L2 miss, executes the valueLoader
	/// to fetch from database
	/// 
	/// The method ensures data consistency by: - Recording cache hits/misses for
	/// performance monitoring - Promoting frequently accessed data from L2 to L1 -
	/// Caching database results in both levels for future access
	/// 
	/// @param <T>         The expected type of the cached value
	/// @param key         The cache key to look up
	/// @param valueLoader Callable that loads the value from database when cache
	///                    misses occur
	/// @return The cached value, or null if type conversion fails
	/// @throws RuntimeException if the valueLoader fails to execute
	@Override
	public <T> T get(Object key, Callable<T> valueLoader) {
		logger.debug("CACHE[{}] - Searching key: {} with valueLoader", name, key);

		// Try L1 cache first (fastest access)
		long startTime = System.nanoTime();
		ValueWrapper value = l1Cache.get(key);
		long l1Time = System.nanoTime() - startTime;

		if (value != null) {
			logger.info("CACHE[{}] - L1 HIT for key: {} ({}ms)", name, key, l1Time / 1_000_000.0);
			if (cacheManager != null) {
				cacheManager.recordL1Hit(name);
			}
			Object cachedValue = value.get();
			if (cachedValue != null) {
				try {
					@SuppressWarnings("unchecked")
					T result = (T) cachedValue;
					return result;
				} catch (ClassCastException e) {
					logger.warn("CACHE[{}] - Type mismatch for key: {}", name, key);
					return null;
				}
			}
		}

		logger.warn("CACHE[{}] - L1 MISS for key: {} ({}ms)", name, key, l1Time / 1_000_000.0);

		// L1 miss, record it and try L2 cache
		if (cacheManager != null) {
			cacheManager.recordL1Miss(name);
		}

		startTime = System.nanoTime();
		value = l2Cache.get(key);
		long l2Time = System.nanoTime() - startTime;

		if (value != null) {
			logger.info("CACHE[{}] - L2 HIT for key: {} ({}ms) - PROMOTING to L1", name, key, l2Time / 1_000_000.0);
			if (cacheManager != null) {
				cacheManager.recordL2Hit(name);
			}
			Object cachedValue = value.get();
			if (cachedValue != null) {
				// Promote to L1 cache for faster future access
				l1Cache.put(key, cachedValue);
				try {
					@SuppressWarnings("unchecked")
					T result = (T) cachedValue;
					return result;
				} catch (ClassCastException e) {
					logger.warn("CACHE[{}] - Type mismatch for key: {}", name, key);
					return null;
				}
			}
		}

		logger.warn("CACHE[{}] - L2 MISS for key: {} ({}ms) - LOADING FROM DATABASE", name, key, l2Time / 1_000_000.0);

		// L2 miss, load value from database and cache it
		if (cacheManager != null) {
			cacheManager.recordL2Miss(name);
		}

		try {
			startTime = System.nanoTime();
			T loadedValue = valueLoader.call(); // Database call happens here
			long dbTime = System.nanoTime() - startTime;

			logger.info("CACHE[{}] - DATABASE LOAD for key: {} ({}ms) - CACHING in L1 & L2", name, key,
					dbTime / 1_000_000.0);

			// Cache in both levels for future access
			l1Cache.put(key, loadedValue);
			l2Cache.put(key, loadedValue);
			return loadedValue;
		} catch (Exception e) {
			logger.error("CACHE[{}] - Failed to load value for key: {}", name, key, e);
			throw new RuntimeException("Failed to load value for key: " + key, e);
		}
	}

	@Override
	public void put(Object key, Object value) {
		logger.debug("CACHE[{}] - PUT key: {} in both L1 & L2", name, key);
		// Store in L1 cache if available
		if (l1Cache != null) {
			l1Cache.put(key, value);
		}
		// Store in L2 cache if available
		if (l2Cache != null) {
			l2Cache.put(key, value);
		}
	}

	@Override
	public ValueWrapper putIfAbsent(Object key, Object value) {
		// Try L1 first if available
		if (l1Cache != null) {
			ValueWrapper existing = l1Cache.get(key);
			if (existing != null) {
				if (cacheManager != null) {
					cacheManager.recordL1Hit(name);
				}
				return existing;
			}
		}

		// Try L2 if available
		if (l2Cache != null) {
			ValueWrapper existing = l2Cache.get(key);
			if (existing != null) {
				if (cacheManager != null) {
					cacheManager.recordL2Hit(name);
				}
				// Promote to L1 if available
				if (l1Cache != null) {
					l1Cache.put(key, existing.get());
				}
				return existing;
			}
		}

		// Not found in either cache, put in both if available
		if (cacheManager != null) {
			cacheManager.recordL1Miss(name);
			cacheManager.recordL2Miss(name);
		}

		ValueWrapper wrapper = new SimpleValueWrapper(value);
		if (l1Cache != null) {
			l1Cache.put(key, wrapper);
		}
		if (l2Cache != null) {
			l2Cache.put(key, wrapper);
		}
		return wrapper;
	}

	@Override
	public void evict(Object key) {
		logger.info("CACHE[{}] - EVICT key: {} from available caches", name, key);
		// Evict from both caches if they exist
		if (l1Cache != null) {
			l1Cache.evict(key);
		}
		if (l2Cache != null) {
			l2Cache.evict(key);
		}
	}

	@Override
	public void clear() {
		logger.info("CACHE[{}] - CLEAR available caches", name);
		// Clear both caches if they exist
		if (l1Cache != null) {
			l1Cache.clear();
		}
		if (l2Cache != null) {
			l2Cache.clear();
		}
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj;
	}

	@Override
	public int hashCode() {
		return System.identityHashCode(this);
	}
}

package com.purbarun.employee.service.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Qualifier;
import com.purbarun.employee.repository.EmployeeRepository;
import java.util.List;

/**
 * Scheduler for periodic cache refresh operations. Handles automatic refresh of
 * L1 and L2 caches through the fallback cache manager.
 */
@Component
public class CacheRefreshScheduler {
	private static final Logger logger = LoggerFactory.getLogger(CacheRefreshScheduler.class);
	private final EmployeeRepository employeeRepository;
	private final TwoLevelCacheManagerImpl twoLevelCacheManager;

	public CacheRefreshScheduler(EmployeeRepository employeeRepository, @Qualifier("twoLevelCacheManager") TwoLevelCacheManagerImpl twoLevelCacheManager) {
		this.employeeRepository = employeeRepository;
		this.twoLevelCacheManager = twoLevelCacheManager;
	}

	/**
	 * Note: L1 cache is NOT refreshed by scheduler. L1 cache uses TTL-based
	 * expiration (5 minutes) and will be refreshed on-demand. This ensures optimal
	 * performance by avoiding unnecessary cache clears.
	 */

	/**
	 * Refresh L2 cache from database using configurable CRON schedule. This ensures
	 * data freshness when database is modified externally. Default CRON: "0 0/30 *
	 * * * *" = Every 30 minutes
	 */
	@Scheduled(cron = "0 0/30 * * * *")
	public void refreshL2CacheFromDatabase() {
		logger.info("Starting scheduled L2 cache refresh from database");

		try {
			// Clear L2 caches first
			twoLevelCacheManager.refreshL2("l2Employees");
			twoLevelCacheManager.refreshL2("l2EmployeeById");
			twoLevelCacheManager.refreshL2("l2EmployeesByDepartment");
			twoLevelCacheManager.refreshL2("l2EmployeeByEmail");
			twoLevelCacheManager.refreshL2("l2SearchResults");
			twoLevelCacheManager.refreshL2("l2Addresses");
			twoLevelCacheManager.refreshL2("l2Statistics");

			// Reload data from database to populate L2 caches
			reloadL2CachesFromDatabase();

			logger.info("L2 cache refresh from database completed successfully");
		} catch (Exception e) {
			logger.error("Failed to refresh L2 cache from database", e);
		}
	}

	/**
	 * Log cache statistics using configurable interval. Only enabled when
	 * cache.statistics.enabled=true
	 */
	@Scheduled(fixedRate = 900000) // 15 minutes
	public void logCacheStatistics() {
		// Check if statistics logging is enabled
		if (!Boolean.parseBoolean(System.getProperty("cache.statistics.enabled", "false"))) {
			return;
		}

		try {
			String stats = twoLevelCacheManager.getAllStatistics();
			logger.info("Cache Statistics:\n{}", stats);
		} catch (Exception e) {
			logger.error("Failed to log cache statistics", e);
		}
	}

	/**
	 * Health check for cache systems using configurable interval.
	 */
	@Scheduled(fixedRate = 300000) // 5 minutes
	public void cacheHealthCheck() {
		try {
			// Simple health check - try to get cache names
			var cacheNames = twoLevelCacheManager.getCacheNames();
			logger.debug("Cache health check passed - {} caches available", cacheNames.size());
		} catch (Exception e) {
			logger.error("Cache health check failed", e);
		}
	}

	/**
	 * Manual trigger for L1 cache refresh (for emergency use only). Normally L1
	 * cache should be managed by TTL, not by scheduler.
	 */
	public void refreshL1Manually() {
		logger.info("Manual L1 cache refresh triggered (emergency use only)");
		try {
			twoLevelCacheManager.refreshL1("l1Employees");
			twoLevelCacheManager.refreshL1("l1EmployeeById");
			twoLevelCacheManager.refreshL1("l1EmployeesByDepartment");
			twoLevelCacheManager.refreshL1("l1EmployeeByEmail");
			twoLevelCacheManager.refreshL1("l1SearchResults");
			twoLevelCacheManager.refreshL1("l1Addresses");
			twoLevelCacheManager.refreshL1("l1Statistics");
			logger.info("Manual L1 cache refresh completed");
		} catch (Exception e) {
			logger.error("Failed to perform manual L1 cache refresh", e);
		}
	}

	/**
	 * Manual trigger for L2 cache refresh from database.
	 */
	public void refreshL2Manually() {
		logger.info("Manual L2 cache refresh from database triggered");
		refreshL2CacheFromDatabase();
	}

	/**
	 * Manual trigger for full cache refresh from database.
	 */
	public void refreshAllManually() {
		logger.info("Manual full cache refresh from database triggered");
		try {
			// Clear all L1 caches
			twoLevelCacheManager.refreshL1("l1Employees");
			twoLevelCacheManager.refreshL1("l1EmployeeById");
			twoLevelCacheManager.refreshL1("l1EmployeesByDepartment");
			twoLevelCacheManager.refreshL1("l1EmployeeByEmail");
			twoLevelCacheManager.refreshL1("l1SearchResults");
			twoLevelCacheManager.refreshL1("l1Addresses");
			twoLevelCacheManager.refreshL1("l1Statistics");

			// Clear and reload L2 caches
			refreshL2CacheFromDatabase();

			logger.info("Full cache refresh from database completed successfully");
		} catch (Exception e) {
			logger.error("Failed to perform full cache refresh from database", e);
			throw new RuntimeException("Failed to refresh cache", e);
		}
	}

	/**
	 * Reload L2 caches with fresh data from database. This method directly accesses
	 * repository to bypass cache and populate L2 cache.
	 */
	private void reloadL2CachesFromDatabase() {
		logger.info("Reloading L2 caches with fresh database data");

		try {
			// Directly access repository to bypass cache
			List<com.purbarun.employee.model.Employee> employees = (List<com.purbarun.employee.model.Employee>) employeeRepository
					.findAll();
			logger.info("Fetched {} employees from database", employees.size());

			// Get L2 cache directly and populate it
			var l2EmployeesCache = twoLevelCacheManager.getCache("employees");
			var l2EmployeeByIdCache = twoLevelCacheManager.getCache("employeeById");

			// Populate employees list cache
			for (com.purbarun.employee.model.Employee employee : employees) {
				l2EmployeesCache.put(employee, employee);
			}
			logger.info("Populated L2 employees cache with {} entries", employees.size());

			// Populate individual employee cache
			for (com.purbarun.employee.model.Employee employee : employees) {
				l2EmployeeByIdCache.put(employee.getId(), employee);
			}
			logger.info("Populated L2 employeeById cache with {} entries", employees.size());

		} catch (Exception e) {
			logger.error("Error reloading L2 caches from database", e);
			throw e;
		}
	}
}

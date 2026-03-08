package com.purbarun.employee.service.cache;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Disabled;

@SpringBootTest
@ActiveProfiles("test")
public class CacheIntegrationTest {

    @Autowired
    private CacheManager cacheManager;

    @Test
    @Disabled
    void testCacheManagerExists() {
        assertNotNull(cacheManager, "CacheManager should be auto-configured");
    }

    @Test
    @Disabled
    void testCacheNamesExist() {
        var cacheNames = cacheManager.getCacheNames();
        assertTrue(cacheNames.contains("employees"), "Cache 'employees' should exist");
        assertTrue(cacheNames.contains("employeeById"), "Cache 'employeeById' should exist");
    }

    @Test
    @Disabled
    void testCacheOperations() {
        var employeesCache = cacheManager.getCache("employees");
        assertNotNull(employeesCache, "Employees cache should exist");
        
        // Test put and get operations
        employeesCache.put("testKey", "testValue");
        var cachedValue = employeesCache.get("testKey");
        assertNotNull(cachedValue, "Cached value should not be null");
        assertEquals("testValue", cachedValue.get(), "Cached value should match");
    }
}

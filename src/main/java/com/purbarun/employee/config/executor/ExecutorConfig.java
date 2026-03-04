package com.purbarun.employee.config.executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Configuration class for centralized executor management.
 * Provides a shared executor for bulk operations across the application.
 */
@Configuration
@EnableAsync
public class ExecutorConfig {
    
    /**
     * Centralized executor configuration for bulk operations.
     * Configured with optimal settings for parallel processing of 10-20 records.
     */
    @Bean(name = "bulkUpdateExecutor")
    ThreadPoolTaskExecutor bulkUpdateExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(6);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("BulkUpdate-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}

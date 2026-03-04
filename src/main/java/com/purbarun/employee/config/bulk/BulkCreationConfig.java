package com.purbarun.employee.config.bulk;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.beans.factory.annotation.Qualifier;

import com.purbarun.employee.repository.EmployeeRepository;
import com.purbarun.employee.service.bulk.AsyncBulkEmployeeCreationService;
import com.purbarun.employee.service.bulk.BatchBulkEmployeeCreationService;
import com.purbarun.employee.service.bulk.BulkEmployeeCreationService;
import com.purbarun.employee.utils.BulkCreationProperties;

import java.util.concurrent.Executor;

/**
 * Configuration class for bulk employee creation strategy selection.
 * Reads the strategy from BulkCreationProperties and provides the appropriate bean.
 */
@Configuration
public class BulkCreationConfig {
    
    private final BulkCreationProperties bulkCreationProperties;
    
    public BulkCreationConfig(BulkCreationProperties bulkCreationProperties) {
        this.bulkCreationProperties = bulkCreationProperties;
    }
    
    /**
     * Creates the async bulk employee creation service bean.
     */
    @Bean
    AsyncBulkEmployeeCreationService asyncBulkEmployeeCreationService(EmployeeRepository employeeRepository, 
                                                           @Qualifier("bulkUpdateExecutor") Executor executor) {
        return new AsyncBulkEmployeeCreationService(employeeRepository, executor);
    }

    /**
     * Creates the batch bulk employee creation service bean.
     */
    @Bean
    BatchBulkEmployeeCreationService batchBulkEmployeeCreationService(EmployeeRepository employeeRepository,
                                                           @Qualifier("bulkUpdateExecutor") Executor executor) {
        return new BatchBulkEmployeeCreationService(employeeRepository, executor);
    }

    /**
     * Creates the appropriate bulk employee creation service based on configuration.
     * 
     * @param asyncService The async implementation
     * @param batchService The batch implementation
     * @return The selected bulk creation service
     */
    @Bean
    @Primary
    BulkEmployeeCreationService bulkEmployeeCreationService(
            AsyncBulkEmployeeCreationService asyncService,
            BatchBulkEmployeeCreationService batchService) {
        
        String strategy = bulkCreationProperties.getBulkCreationStrategy().toUpperCase();
        
        switch (strategy) {
            case "ASYNC":
                return asyncService;
            case "BATCH":
                return batchService;
            default:
                throw new IllegalArgumentException("Unknown bulk creation strategy: " + strategy + 
                    ". Valid options are: ASYNC, BATCH");
        }
    }
    
    /**
     * Returns the current bulk creation strategy.
     * 
     * @return The strategy name
     */
    public String getBulkCreationStrategy() {
        return bulkCreationProperties.getBulkCreationStrategy().toUpperCase();
    }
}

package com.purbarun.employee.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Service to hold bulk creation configuration.
 * Configuration can be refreshed via custom endpoint.
 */
@Component
public class BulkCreationProperties {
    
    @Value("${app.bulk-creation.strategy:ASYNC}")
    private String bulkCreationStrategy;
    
    public String getBulkCreationStrategy() {
        return bulkCreationStrategy;
    }
    
    public void setBulkCreationStrategy(String bulkCreationStrategy) {
        this.bulkCreationStrategy = bulkCreationStrategy;
    }
}

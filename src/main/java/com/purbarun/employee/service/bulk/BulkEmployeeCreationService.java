package com.purbarun.employee.service.bulk;

import java.util.List;

import com.purbarun.employee.dto.EmployeeCreateRequest;
import com.purbarun.employee.enums.BulkCreationStrategy;
import com.purbarun.employee.model.Employee;

/**
 * Sealed interface for bulk employee creation operations.
 * Different implementations provide different strategies for bulk creation.
 * Only AbstractBulkEmployeeCreationService can implement this interface, which then allows the concrete implementations.
 */
public sealed interface BulkEmployeeCreationService 
    permits AbstractBulkEmployeeCreationService {
    
    /**
     * Creates multiple employees in bulk using the specific implementation strategy.
     * 
     * @param createRequests List of employee creation requests
     * @return List of created employees
     * @throws IllegalArgumentException if validation fails
     */
    List<Employee> bulkCreateEmployees(List<EmployeeCreateRequest> createRequests);
    
    /**
     * Returns the strategy type as an enum for type safety.
     * 
     * @return The strategy type enum
     */
    BulkCreationStrategy getStrategy();
}

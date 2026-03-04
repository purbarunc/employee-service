package com.purbarun.employee.service.bulk;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.purbarun.employee.dto.EmployeeCreateRequest;
import com.purbarun.employee.enums.BulkCreationStrategy;
import com.purbarun.employee.model.Employee;
import com.purbarun.employee.repository.EmployeeRepository;

/**
 * Batch bulk employee creation service implementation.
 * Uses batch processing with validation before creation for better performance and consistency.
 */
public class BatchBulkEmployeeCreationService extends AbstractBulkEmployeeCreationService {
    
    private static final Logger logger = LoggerFactory.getLogger(BatchBulkEmployeeCreationService.class);
    
    public BatchBulkEmployeeCreationService(EmployeeRepository employeeRepository, Executor executor) {
        super(employeeRepository, executor);
    }
    
    @Override
    @Transactional
    public List<Employee> bulkCreateEmployees(List<EmployeeCreateRequest> createRequests) {
        logger.info("Starting BATCH bulk creation of {} employees using thread: {}", 
                   createRequests.size(), Thread.currentThread().getName());
        
        long startTime = System.currentTimeMillis();
        
        // First, validate all emails are unique and don't exist
        List<String> emails = createRequests.stream()
            .map(EmployeeCreateRequest::email)
            .collect(Collectors.toList());
        
        // Check for duplicates in the request itself
        validateUniqueEmails(emails);
        
        // Check for existing emails in database
        validateEmailsNotExist(emails);
        
        logger.info("Validation passed for {} employees, proceeding with batch creation", createRequests.size());
        
        // Process creations in parallel using executor service
        List<CompletableFuture<Employee>> futures = createRequests.stream()
            .map(this::createEmployeeAsync)
            .collect(Collectors.toList());
        
        List<Employee> results = futures.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList());
        
        long endTime = System.currentTimeMillis();
        logger.info("Completed BATCH bulk creation of {} employees in {}ms using parallel processing", 
                   results.size(), (endTime - startTime));
        
        return results;
    }
    
    @Override
    public BulkCreationStrategy getStrategy() {
        return BulkCreationStrategy.BATCH;
    }
}

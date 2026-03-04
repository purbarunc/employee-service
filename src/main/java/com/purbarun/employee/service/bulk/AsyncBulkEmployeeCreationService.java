package com.purbarun.employee.service.bulk;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.purbarun.employee.dto.EmployeeCreateRequest;
import com.purbarun.employee.enums.BulkCreationStrategy;
import com.purbarun.employee.model.Employee;
import com.purbarun.employee.repository.EmployeeRepository;

/**
 * Asynchronous bulk employee creation service implementation.
 * Uses CompletableFuture and ExecutorService for parallel processing.
 */
public class AsyncBulkEmployeeCreationService extends AbstractBulkEmployeeCreationService {
    
    private static final Logger logger = LoggerFactory.getLogger(AsyncBulkEmployeeCreationService.class);
    
    public AsyncBulkEmployeeCreationService(EmployeeRepository employeeRepository, java.util.concurrent.Executor executor) {
        super(employeeRepository, executor);
    }
    
    @Override
    @Transactional
    public List<Employee> bulkCreateEmployees(List<EmployeeCreateRequest> createRequests) {
        logger.info("Starting ASYNC bulk creation of {} employees using thread: {}", 
                   createRequests.size(), Thread.currentThread().getName());
        
        long startTime = System.currentTimeMillis();
        
        List<CompletableFuture<Employee>> futures = createRequests.stream()
            .map(this::createEmployeeAsync)
            .collect(Collectors.toList());
        
        List<Employee> results = futures.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList());
        
        long endTime = System.currentTimeMillis();
        logger.info("Completed ASYNC bulk creation of {} employees in {}ms using multiple threads", 
                   results.size(), (endTime - startTime));
        
        return results;
    }
    
    /**
     * Internal method to handle the actual employee creation with email validation
     */
    @Override
    protected Employee createEmployeeInternal(EmployeeCreateRequest request) {
        // Check for duplicate email
        validateEmailNotExists(request.email());
        
        return super.createEmployeeInternal(request);
    }
    
    @Override
    public BulkCreationStrategy getStrategy() {
        return BulkCreationStrategy.ASYNC;
    }
}

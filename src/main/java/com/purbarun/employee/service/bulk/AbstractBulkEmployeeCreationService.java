package com.purbarun.employee.service.bulk;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.purbarun.employee.dto.EmployeeCreateRequest;
import com.purbarun.employee.enums.BulkCreationStrategy;
import com.purbarun.employee.model.Employee;
import com.purbarun.employee.repository.EmployeeRepository;

/**
 * Abstract base class for bulk employee creation services.
 * Contains common functionality to reduce code duplication.
 */
public non-sealed abstract class AbstractBulkEmployeeCreationService implements BulkEmployeeCreationService {
    
    protected static final Logger logger = LoggerFactory.getLogger(AbstractBulkEmployeeCreationService.class);
    
    protected final EmployeeRepository employeeRepository;
    protected final Executor executor;
    
    protected AbstractBulkEmployeeCreationService(EmployeeRepository employeeRepository, Executor executor) {
        this.employeeRepository = Objects.requireNonNull(employeeRepository);
        this.executor = Objects.requireNonNull(executor);
    }
    
    /**
     * Async method to create a single employee
     */
    protected CompletableFuture<Employee> createEmployeeAsync(EmployeeCreateRequest request) {
        String currentThread = Thread.currentThread().getName();
        logger.info("Submitting employee {} for async creation from thread: {}", request.email(), currentThread);
        
        return CompletableFuture.supplyAsync(() -> {
            String workerThread = Thread.currentThread().getName();
            logger.info("Creating employee {} on worker thread: {}", request.email(), workerThread);
            
            try {
                return createEmployeeInternal(request);
            } catch (Exception e) {
                logger.error("Failed to create employee {} on thread {}: {}", 
                           request.email(), workerThread, e.getMessage());
                throw new RuntimeException("Failed to create employee: " + request.email(), e);
            }
        }, executor);
    }
    
    /**
     * Internal method to handle the actual employee creation
     */
    protected Employee createEmployeeInternal(EmployeeCreateRequest request) {
        Employee employee = new Employee();
        employee.setFirstName(request.firstName());
        employee.setLastName(request.lastName());
        employee.setEmail(request.email());
        employee.setPhoneNumber(request.phoneNumber());
        employee.setSalary(request.salary());
        employee.setHireDate(request.hireDate() != null ? request.hireDate() : java.time.LocalDate.now());
        employee.setStatus(request.status() != null ? request.status() : "ACTIVE");
        employee.setManager(request.manager());
        
        var now = LocalDateTime.now();
        employee.setCreatedAt(now);
        employee.setUpdatedAt(now);
        
        return employeeRepository.save(employee);
    }
    
    /**
     * Validates that an email doesn't already exist in the database
     */
    protected void validateEmailNotExists(String email) {
        if (employeeRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Employee with email already exists: " + email);
        }
    }
    
    /**
     * Validates that multiple emails don't already exist in the database
     */
    protected void validateEmailsNotExist(List<String> emails) {
        List<String> existingEmails = employeeRepository.findAllByEmailIn(emails).stream()
            .map(Employee::getEmail)
            .toList();
        
        if (!existingEmails.isEmpty()) {
            throw new IllegalArgumentException("Employees with emails already exist: " + existingEmails);
        }
    }
    
    /**
     * Validates that emails in a list are unique (no duplicates within the list)
     */
    protected void validateUniqueEmails(List<String> emails) {
        List<String> duplicateEmails = emails.stream()
            .filter(email -> emails.stream().filter(e -> e.equals(email)).count() > 1)
            .distinct()
            .toList();
        
        if (!duplicateEmails.isEmpty()) {
            throw new IllegalArgumentException("Duplicate emails in request: " + duplicateEmails);
        }
    }
    
    /**
     * Abstract method to be implemented by concrete classes to return their strategy type
     */
    public abstract BulkCreationStrategy getStrategy();
}

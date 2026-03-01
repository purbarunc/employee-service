package com.purbarun.employee.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.purbarun.employee.model.Employee;
import com.purbarun.employee.repository.EmployeeRepository;

@Service
public class EmployeeService {

	private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

	private final EmployeeRepository employeeRepository;

	// Constructor injection (preferred)
	public EmployeeService(EmployeeRepository employeeRepository) {
		this.employeeRepository = Objects.requireNonNull(employeeRepository);
	}

	// Create
	@Transactional
	public Employee createEmployee(Employee employee) {
		var now = LocalDateTime.now();
		employee.setCreatedAt(now);
		employee.setUpdatedAt(now);

		Employee savedEmployee = employeeRepository.save(employee);

		logger.info("Created and cached employee with ID: {}", savedEmployee.getId());
		return savedEmployee;
	}

	// Read single - returns Optional so callers can decide how to handle absence
	public Optional<Employee> getEmployee(Long id) {
		logger.debug("Fetching employee {} from database", id);
		return employeeRepository.findById(id);
	}

	// Read all
	public List<Employee> getAllEmployees() {
		logger.debug("Fetching all employees from database");
		return (List<Employee>) employeeRepository.findAll();
	}

	// Update - transactional to ensure consistency
	@Transactional
	public Employee updateEmployee(Long id, Employee updated) {
		logger.debug("Updating employee with ID: {}", id);

		Employee updatedEmployee = employeeRepository.findById(id).map(existing -> {
			// copy updatable fields
			existing.setFirstName(updated.getFirstName());
			existing.setLastName(updated.getLastName());
			existing.setEmail(updated.getEmail());
			existing.setPhoneNumber(updated.getPhoneNumber());
			existing.setSalary(updated.getSalary());
			existing.setHireDate(updated.getHireDate());
			existing.setStatus(updated.getStatus());
			existing.setManager(updated.getManager());
			existing.setAddresses(updated.getAddresses());
			existing.setUpdatedAt(LocalDateTime.now());
			return employeeRepository.save(existing);
		}).orElseThrow(() -> new IllegalArgumentException("Employee not found: " + id));

		logger.info("Updated and cached employee with ID: {}", id);
		return updatedEmployee;
	}

	// Delete
	@Transactional
	public void deleteEmployee(Long id) {
		logger.debug("Deleting employee with ID: {}", id);

		if (!employeeRepository.existsById(id)) {
			throw new IllegalArgumentException("Employee not found: " + id);
		}

		employeeRepository.deleteById(id);
		logger.info("Deleted and evicted from cache employee with ID: {}", id);
	}
}
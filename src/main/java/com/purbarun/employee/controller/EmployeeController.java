package com.purbarun.employee.controller;

import java.net.URI;
import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.purbarun.employee.dto.BulkEmployeeCreateRequest;
import com.purbarun.employee.model.Address;
import com.purbarun.employee.model.Employee;
import com.purbarun.employee.service.AddressService;
import com.purbarun.employee.service.EmployeeService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class EmployeeController {
	private final EmployeeService employeeService;
	private final AddressService addressService;

	// Constructor injection
	public EmployeeController(EmployeeService employeeService, AddressService addressService) {
		this.employeeService = Objects.requireNonNull(employeeService);
		this.addressService = Objects.requireNonNull(addressService);
	}

	// Create - returns 201 Created with Location header
	@PostMapping("/employee")
	public ResponseEntity<Employee> createEmployee(@Valid @RequestBody Employee employee) {
		var created = employeeService.createEmployee(employee);
		var location = URI.create(String.format("/api/employees/%d", created.getId()));
		return ResponseEntity.created(location).body(created);
	}
	
	// Create addresses for employee
	@PostMapping("/employee/{employeeId}/addresses")
	public ResponseEntity<List<Address>> createAddressesForEmployee(@PathVariable("employeeId") Long employeeId,
			@Valid @RequestBody List<Address> addresses) {
		try {
			var created = addressService.createAddressesForEmployee(employeeId, addresses);
			return ResponseEntity.ok(created);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.notFound().build();
		}
	}

	// Read single - returns 200 OK or 404 Not Found
	@GetMapping("/employees/{id}")
	public ResponseEntity<Employee> getEmployee(@PathVariable("id") Long id) {
		var opt = employeeService.getEmployee(id);
		return ResponseEntity.of(opt);
	}

	// Read all - returns 200 OK
	@GetMapping("/employees")
	public ResponseEntity<Iterable<Employee>> getAllEmployees() {
		var all = employeeService.getAllEmployees();
		return ResponseEntity.ok(all);
	}

	// Update (full) - returns 200 OK or 404 Not Found
	@PutMapping("/employees/{id}")
	public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @Valid @RequestBody Employee employee) {
		try {
			var updated = employeeService.updateEmployee(id, employee);
			return ResponseEntity.ok(updated);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.notFound().build();
		}
	}

	// Delete - returns 204 No Content or 404 Not Found
	@DeleteMapping("/employees/{id}")
	public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
		try {
			employeeService.deleteEmployee(id);
			return ResponseEntity.noContent().build();
		} catch (IllegalArgumentException e) {
			return ResponseEntity.notFound().build();
		}
	}
	
	@PostMapping("/employees")
	public ResponseEntity<?> bulkCreateEmployees(@Valid @RequestBody BulkEmployeeCreateRequest bulkRequest) {
	    try {
	        List<Employee> createdEmployees = employeeService.bulkCreateEmployees(bulkRequest.employees());
	        return ResponseEntity.status(HttpStatus.CREATED).body(createdEmployees);
	    } catch (IllegalArgumentException e) {
	        return ResponseEntity.badRequest().body(e.getMessage());
	    } catch (Exception e) {
	        return ResponseEntity.internalServerError().body("Bulk creation failed: " + e.getMessage());
	    }
	}
}
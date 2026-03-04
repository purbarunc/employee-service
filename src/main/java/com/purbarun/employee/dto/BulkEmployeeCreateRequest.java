package com.purbarun.employee.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record BulkEmployeeCreateRequest(
	@Valid
    @NotEmpty(message = "Employee list cannot be empty")
    @Size(min = 1, max = 20, message = "Bulk creation must contain between 1 and 20 employees")
    List<EmployeeCreateRequest> employees
) {
}

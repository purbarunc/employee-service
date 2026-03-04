package com.purbarun.employee.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record EmployeeCreateRequest(
	@NotBlank(message = "First name is required") String firstName,
	@NotBlank(message = "Last name is required") String lastName,
	@NotBlank(message = "Email is required") @Email(message = "Email should be valid") String email,
	String phoneNumber,
	@NotNull(message = "Salary is required") @Positive(message = "Salary must be positive") Double salary,
	LocalDate hireDate,
	String status,
	String manager
) {
}

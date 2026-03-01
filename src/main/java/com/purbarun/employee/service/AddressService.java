package com.purbarun.employee.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.purbarun.employee.model.Address;
import com.purbarun.employee.model.Employee;
import com.purbarun.employee.repository.AddressRepository;
import com.purbarun.employee.repository.EmployeeRepository;

@Service
public class AddressService {
	private final AddressRepository addressRepository;
	private final EmployeeRepository employeeRepository;

	public AddressService(AddressRepository addressRepository, EmployeeRepository employeeRepository) {
		this.addressRepository = Objects.requireNonNull(addressRepository);
		this.employeeRepository = Objects.requireNonNull(employeeRepository);
	}

	@Transactional
	public List<Address> createAddressesForEmployee(Long employeeId, List<Address> addresses) {
		Employee employee = employeeRepository.findById(employeeId)
				.orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));

		var now = LocalDateTime.now();
		for (Address address : addresses) {
			address.setEmployee(employee);
			address.setCreatedAt(now);
			address.setUpdatedAt(now);
		}

		return (List<Address>) addressRepository.saveAll(addresses);
	}
}

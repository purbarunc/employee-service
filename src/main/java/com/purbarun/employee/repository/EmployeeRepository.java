package com.purbarun.employee.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.purbarun.employee.model.Employee;

@Repository
public interface EmployeeRepository extends CrudRepository<Employee, Long> {
	List<Employee> findAllByEmailIn(List<String> emails);

	boolean existsByEmail(String email);

}

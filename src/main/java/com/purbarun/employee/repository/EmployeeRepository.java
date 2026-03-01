package com.purbarun.employee.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.purbarun.employee.model.Employee;

@Repository
public interface EmployeeRepository extends CrudRepository<Employee, Long> {
}

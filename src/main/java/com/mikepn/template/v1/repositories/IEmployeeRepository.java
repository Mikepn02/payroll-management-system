package com.mikepn.template.v1.repositories;

import com.mikepn.template.v1.models.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IEmployeeRepository extends JpaRepository<Employee , UUID> {
}

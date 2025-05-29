package com.mikepn.template.v1.services;

import com.mikepn.template.v1.dtos.request.employee.CreateEmployeeDTO;
import com.mikepn.template.v1.dtos.request.employee.UpdateEmployeeDTO;
import com.mikepn.template.v1.dtos.response.employee.EmployeeResponseDTO;

import java.util.List;
import java.util.UUID;

public interface IEmployementService {


    EmployeeResponseDTO createEmployee(CreateEmployeeDTO createEmployeeDTO);
    EmployeeResponseDTO getEmployeeById(UUID employeeId);
    void deleteEmployee(UUID employeeId);
    List<EmployeeResponseDTO> getAllEmployees();
    EmployeeResponseDTO updateEmployee(UUID employeeId, UpdateEmployeeDTO updateEmployeeDTO);

}

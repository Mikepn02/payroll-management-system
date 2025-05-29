package com.mikepn.template.v1.controllers;

import com.mikepn.template.v1.dtos.request.employee.CreateEmployeeDTO;
import com.mikepn.template.v1.dtos.request.employee.UpdateEmployeeDTO;
import com.mikepn.template.v1.dtos.response.employee.EmployeeResponseDTO;
import com.mikepn.template.v1.payload.ApiResponse;
import com.mikepn.template.v1.services.IEmployementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("employees")
@RequiredArgsConstructor
@Tag(name = "Employee Management", description = "Endpoints for managing employees")
public class EmployeeController {

    private final IEmployementService employementService;
    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);

    @Operation(summary = "Create new employee", description = "Creates a new employee record")
    @PostMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeResponseDTO>> createEmployee(
            @Valid @RequestBody CreateEmployeeDTO createEmployeeDTO) {
        try {
            logger.debug("Creating new employee with email: {}", createEmployeeDTO.getEmail());
            EmployeeResponseDTO response = employementService.createEmployee(createEmployeeDTO);
            return ApiResponse.success("Employee created successfully", HttpStatus.CREATED, response);
        } catch (Exception e) {
            logger.error("Failed to create employee", e);
            return ApiResponse.fail("Failed to create employee", HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Operation(summary = "Get all employees", description = "Retrieves a list of all employees")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<EmployeeResponseDTO>>> getAllEmployees() {
        try {
            logger.debug("Retrieving all employees");
            List<EmployeeResponseDTO> employees = employementService.getAllEmployees();
            return ApiResponse.success("Employees retrieved successfully", HttpStatus.OK, employees);
        } catch (Exception e) {
            logger.error("Failed to retrieve employees", e);
            return ApiResponse.fail("Failed to retrieve employees", HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Operation(summary = "Get employee by ID", description = "Retrieves an employee by their ID")
    @GetMapping("/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeResponseDTO>> getEmployeeById(
            @PathVariable UUID employeeId) {
        try {
            logger.debug("Retrieving employee with ID: {}", employeeId);
            EmployeeResponseDTO employee = employementService.getEmployeeById(employeeId);
            return ApiResponse.success("Employee retrieved successfully", HttpStatus.OK, employee);
        } catch (Exception e) {
            logger.error("Failed to retrieve employee with ID: {}", employeeId, e);
            return ApiResponse.fail("Failed to retrieve employee", HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Operation(summary = "Update employee", description = "Updates an existing employee's information")
    @PutMapping("/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeResponseDTO>> updateEmployee(
            @PathVariable UUID employeeId,
            @Valid @RequestBody UpdateEmployeeDTO updateEmployeeDTO) {
        try {
            logger.debug("Updating employee with ID: {}", employeeId);
            EmployeeResponseDTO updatedEmployee = employementService.updateEmployee(employeeId, updateEmployeeDTO);
            return ApiResponse.success("Employee updated successfully", HttpStatus.OK, updatedEmployee);
        } catch (Exception e) {
            logger.error("Failed to update employee with ID: {}", employeeId, e);
            return ApiResponse.fail("Failed to update employee", HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Operation(summary = "Delete employee", description = "Deletes an employee record")
    @DeleteMapping("/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(@PathVariable UUID employeeId) {
        try {
            logger.debug("Deleting employee with ID: {}", employeeId);
            employementService.deleteEmployee(employeeId);
            return ApiResponse.success("Employee deleted successfully", HttpStatus.NO_CONTENT, null);
        } catch (Exception e) {
            logger.error("Failed to delete employee with ID: {}", employeeId, e);
            return ApiResponse.fail("Failed to delete employee", HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
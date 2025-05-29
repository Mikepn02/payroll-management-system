package com.mikepn.template.v1.controllers;

import com.mikepn.template.v1.dtos.request.PayslipRequestDTO;
import com.mikepn.template.v1.dtos.response.payslip.PayslipResponseDTO;
import com.mikepn.template.v1.payload.ApiResponse;
import com.mikepn.template.v1.services.IPaySlipService;
import com.mikepn.template.v1.utils.UserUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("payslips")
@RequiredArgsConstructor
@Tag(name = "Payslip Management", description = "Endpoints for managing employee payslips")
public class PayslipController {

    private final IPaySlipService paySlipService;
    private final UserUtils userUtils;  // Injected as instance bean
    private static final Logger logger = LoggerFactory.getLogger(PayslipController.class);

    @Operation(summary = "Generate payslips for all active employees")
    @PostMapping("/generate")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<PayslipResponseDTO>>> generatePayslips(
            @Valid @RequestBody PayslipRequestDTO requestDTO) {
        try {
            logger.info("Generating payslips for month: {} year: {}", requestDTO.getMonth(), requestDTO.getYear());
            List<PayslipResponseDTO> payslips = paySlipService.generatePaysSlips(requestDTO);
            return ApiResponse.success("Payslips generated successfully", HttpStatus.CREATED, payslips);
        } catch (Exception e) {
            logger.error("Failed to generate payslips", e);
            return ApiResponse.fail("Failed to generate payslips", HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Operation(summary = "Get a specific payslip by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<PayslipResponseDTO>> getPayslipById(@PathVariable UUID id) {
        try {
            // For EMPLOYEE role, ensure they can only view their own payslips
            if (userUtils.hasRole("ROLE_EMPLOYEE") && !userUtils.isPayslipOwner(id)) {
                return ApiResponse.fail("Access denied", HttpStatus.FORBIDDEN, "You can only view your own payslips");
            }

            PayslipResponseDTO payslip = paySlipService.getPayslipById(id);
            return ApiResponse.success("Payslip retrieved successfully", HttpStatus.OK, payslip);
        } catch (Exception e) {
            logger.error("Failed to retrieve payslip with ID: {}", id, e);
            return ApiResponse.fail("Failed to retrieve payslip", HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Operation(summary = "Get all payslips for a specific employee")
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<PayslipResponseDTO>>> getPayslipsByEmployee(@PathVariable UUID employeeId) {
        try {
            List<PayslipResponseDTO> payslips = paySlipService.getPayslipsByEmployeeId(employeeId);
            return ApiResponse.success("Payslips retrieved successfully", HttpStatus.OK, payslips);
        } catch (Exception e) {
            logger.error("Failed to retrieve payslips for employee: {}", employeeId, e);
            return ApiResponse.fail("Failed to retrieve payslips", HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Operation(summary = "Get current employee's payslips")
    @GetMapping("/my-payslips")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<PayslipResponseDTO>>> getMyPayslips() {
        try {
            UUID employeeId = userUtils.getCurrentEmployeeId();
            List<PayslipResponseDTO> payslips = paySlipService.getPayslipsByEmployeeId(employeeId);
            return ApiResponse.success("Your payslips retrieved successfully", HttpStatus.OK, payslips);
        } catch (Exception e) {
            logger.error("Failed to retrieve current employee's payslips", e);
            return ApiResponse.fail("Failed to retrieve payslips", HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Operation(summary = "Get specific month/year payslip for current employee")
    @GetMapping("/my-payslip/{month}/{year}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<PayslipResponseDTO>> getMyPayslipForMonth(
            @PathVariable Integer month, @PathVariable Integer year) {
        try {
            UUID employeeId = userUtils.getCurrentEmployeeId();
            PayslipResponseDTO payslip = paySlipService.getEmployeePayslipForMonthYear(employeeId, month, year);
            return ApiResponse.success("Payslip retrieved successfully", HttpStatus.OK, payslip);
        } catch (Exception e) {
            logger.error("Failed to retrieve payslip for month: {} year: {}", month, year, e);
            return ApiResponse.fail("Failed to retrieve payslip", HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Operation(summary = "Get all payslips for a specific month and year")
    @GetMapping("/month/{month}/year/{year}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<PayslipResponseDTO>>> getPayslipsByMonthAndYear(
            @PathVariable Integer month, @PathVariable Integer year) {
        try {
            List<PayslipResponseDTO> payslips = paySlipService.getPayslipsByMonthAndYear(month, year);
            return ApiResponse.success("Payslips retrieved successfully", HttpStatus.OK, payslips);
        } catch (Exception e) {
            logger.error("Failed to retrieve payslips for month: {} year: {}", month, year, e);
            return ApiResponse.fail("Failed to retrieve payslips", HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Operation(summary = "Approve a specific payslip")
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PayslipResponseDTO>> approvePayslip(@PathVariable UUID id) {
        try {
            PayslipResponseDTO payslip = paySlipService.approvePayslip(id);
            return ApiResponse.success("Payslip approved successfully", HttpStatus.OK, payslip);
        } catch (Exception e) {
            logger.error("Failed to approve payslip with ID: {}", id, e);
            return ApiResponse.fail("Failed to approve payslip", HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Operation(summary = "Approve all payslips for a specific month and year")
    @PutMapping("/month/{month}/year/{year}/approve-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PayslipResponseDTO>>> approveAllPayslips(
            @PathVariable Integer month, @PathVariable Integer year) {
        try {
            List<PayslipResponseDTO> payslips = paySlipService.approveAllPayslips(month, year);
            return ApiResponse.success("All payslips approved successfully", HttpStatus.OK, payslips);
        } catch (Exception e) {
            logger.error("Failed to approve payslips for month: {} year: {}", month, year, e);
            return ApiResponse.fail("Failed to approve payslips", HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}

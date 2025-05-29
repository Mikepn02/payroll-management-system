package com.mikepn.template.v1.controllers;

import com.mikepn.template.v1.dtos.request.deductions.CreateDeductionDTO;
import com.mikepn.template.v1.dtos.request.deductions.UpdateDeductionDTO;
import com.mikepn.template.v1.models.Deduction;
import com.mikepn.template.v1.payload.ApiResponse;
import com.mikepn.template.v1.services.IDeductionService;
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
@RequestMapping("deductions")
@RequiredArgsConstructor
@Tag(name = "Deduction Management", description = "Endpoints for managing salary deductions")
public class DeductionController {

    private final IDeductionService deductionService;
    private static final Logger logger = LoggerFactory.getLogger(DeductionController.class);

    @Operation(summary = "Create a new deduction")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Deduction>> createDeduction(
            @Valid @RequestBody CreateDeductionDTO dto) {
        try {
            logger.debug("Creating deduction: {}", dto);
            Deduction deduction = deductionService.createDeduction(dto);
            return ApiResponse.success("Deduction created successfully", HttpStatus.CREATED, deduction);
        } catch (Exception e) {
            logger.error("Failed to create deduction", e);
            return ApiResponse.fail("Deduction creation failed", HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Operation(summary = "Get all deductions")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Deduction>>> getAllDeductions() {
        try {
            List<Deduction> deductions = deductionService.getAllDeductions();
            return ApiResponse.success("Deductions retrieved successfully", HttpStatus.OK, deductions);
        } catch (Exception e) {
            logger.error("Failed to retrieve deductions", e);
            return ApiResponse.fail("Failed to retrieve deductions", HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Operation(summary = "Get a deduction by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Deduction>> getDeductionById(@PathVariable UUID id) {
        try {
            Deduction deduction = deductionService.getDeductionById(id);
            return ApiResponse.success("Deduction retrieved successfully", HttpStatus.OK, deduction);
        } catch (Exception e) {
            logger.error("Failed to retrieve deduction with ID: {}", id, e);
            return ApiResponse.fail("Deduction not found", HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @Operation(summary = "Get a deduction by code")
    @GetMapping("/code/{code}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Deduction>> getDeductionByCode(@PathVariable String code) {
        try {
            Deduction deduction = deductionService.getDeductionByCode(code);
            return ApiResponse.success("Deduction retrieved successfully", HttpStatus.OK, deduction);
        } catch (Exception e) {
            logger.error("Failed to retrieve deduction with code: {}", code, e);
            return ApiResponse.fail("Deduction not found", HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @Operation(summary = "Update a deduction")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Deduction>> updateDeduction(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDeductionDTO dto) {
        try {
            Deduction updated = deductionService.updateDeduction(id, dto);
            return ApiResponse.success("Deduction updated successfully", HttpStatus.OK, updated);
        } catch (Exception e) {
            logger.error("Failed to update deduction with ID: {}", id, e);
            return ApiResponse.fail("Deduction update failed", HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Operation(summary = "Delete a deduction")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDeduction(@PathVariable UUID id) {
        try {
            deductionService.deleteDeduction(id);
            return ApiResponse.success("Deduction deleted successfully", HttpStatus.NO_CONTENT, null);
        } catch (Exception e) {
            logger.error("Failed to delete deduction with ID: {}", id, e);
            return ApiResponse.fail("Deduction deletion failed", HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}

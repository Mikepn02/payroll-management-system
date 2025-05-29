package com.mikepn.template.v1.dtos.request.deduction;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateDeductionDTO {

    @Schema(example = "EMPLOYEE_TAX")
    private String deductionName;

    @Schema(example = "30.00")
    @DecimalMin(value = "0.01", message = "Percentage must be greater than 0")
    @DecimalMax(value = "100.00", message = "Percentage cannot be greater than 100")
    private BigDecimal percentage;
}
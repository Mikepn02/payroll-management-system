package com.mikepn.template.v1.dtos.request.deduction;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateDeductionDTO {

    @Schema(example = "EMP_TAX")
    @NotBlank(message = "Deduction name cannot be blank")
    private String deductionName;

    @Schema(example = "30.00")
    @NotNull(message = "Percentage cannot be null")
    @DecimalMin(value = "0.01", message = "Percentage must be greater than 0")
    @DecimalMax(value = "100.00", message = "Percentage cannot be greater than 100")
    private BigDecimal percentage;
}
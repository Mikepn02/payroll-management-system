package com.mikepn.template.v1.dtos.request.employee;

import com.mikepn.template.v1.dtos.request.auth.RegisterUserDTO;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateEmployeeDTO extends RegisterUserDTO {

    @NotBlank(message = "Department is required")
    private String department;

    @NotBlank(message = "Position is required")
    private String position;

    @NotNull(message = "Base salary is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Base salary must be greater than 0")
    private BigDecimal baseSalary;

    @NotNull(message = "Joining date is required")
    private LocalDate joiningDate;

}

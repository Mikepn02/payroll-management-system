package com.mikepn.template.v1.dtos.response.payslip;

import com.mikepn.template.v1.enums.EPaySlipStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayslipResponseDTO {
    private UUID id;
    private String employeeCode;
    private String employeeName;
    private BigDecimal baseSalary;
    private BigDecimal houseAmount;
    private BigDecimal transportAmount;
    private BigDecimal grossSalary;
    private BigDecimal employeeTaxAmount;
    private BigDecimal pensionAmount;
    private BigDecimal medicalInsuranceAmount;
    private BigDecimal otherTaxAmount;
    private BigDecimal netSalary;
    private EPaySlipStatus status;
    private Integer month;
    private Integer year;
    private String monthName;
    private LocalDateTime approvedAt;
    private String approvedBy;
}
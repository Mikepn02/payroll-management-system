package com.mikepn.template.v1.utils.helper;

import com.mikepn.template.v1.dtos.response.payslip.PayslipResponseDTO;
import com.mikepn.template.v1.exceptions.AppException;
import com.mikepn.template.v1.models.Deduction;
import com.mikepn.template.v1.models.Employee;
import com.mikepn.template.v1.models.Payslip;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;


@Service
public class PayslipHelper {

    public Deduction findDeductionByName(List<Deduction> deductions, String name) {
        return deductions.stream()
                .filter(d -> d.getDeductionName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new AppException("Deduction not found: " + name));
    }

    public BigDecimal calculatePercentage(BigDecimal amount, BigDecimal percentage) {
        return amount.multiply(percentage.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public PayslipResponseDTO mapToPayslipResponseDTO(Payslip payslip) {
        Employee employee = payslip.getEmployee();

        return PayslipResponseDTO.builder()
                .id(payslip.getId())
                .employeeCode(employee.getCode())
                .employeeName(employee.getProfile().getFirstName() + " " + employee.getProfile().getLastName())
                .baseSalary(payslip.getBaseSalary())
                .houseAmount(payslip.getHouseAmount())
                .transportAmount(payslip.getTransportAmount())
                .grossSalary(payslip.getGrossSalary())
                .employeeTaxAmount(payslip.getEmployeeTaxAmount())
                .pensionAmount(payslip.getPensionAmount())
                .medicalInsuranceAmount(payslip.getMedicalInsuranceAmount())
                .otherTaxAmount(payslip.getOtherTaxAmount())
                .netSalary(payslip.getNetSalary())
                .status(payslip.getStatus())
                .month(payslip.getMonth())
                .year(payslip.getYear())
                .monthName(payslip.getMonthName())
                .approvedAt(payslip.getApprovedAt())
                .approvedBy(payslip.getApprovedBy())
                .build();
    }

}

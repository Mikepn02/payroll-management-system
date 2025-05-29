package com.mikepn.template.v1.dtos.response.employee;

import com.mikepn.template.v1.enums.EmployementStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;


@Builder
@Data
public class EmployeeResponseDTO {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate dateOfBirth;
    private boolean verified;
    private String code;
    private String phoneNumber;
    private String department;
    private String position;
    private BigDecimal baseSalary;
    private EmployementStatus employmentStatus;
    private LocalDate joiningDate;


}

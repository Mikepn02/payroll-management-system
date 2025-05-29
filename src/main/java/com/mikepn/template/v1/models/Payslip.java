package com.mikepn.template.v1.models;

import com.mikepn.template.v1.common.AbstractEntity;
import com.mikepn.template.v1.enums.EPaySlipStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "payslips")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Payslip extends AbstractEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "house_amount", precision = 15, scale = 2)
    private BigDecimal houseAmount;


    @Column(name = "transport_amount", precision = 15, scale = 2)
    private BigDecimal transportAmount;

    @Column(name = "employee_tax_amount", precision = 15, scale = 2)
    private BigDecimal employeeTaxAmount;


    @Column(name = "pension_amount", precision = 15, scale = 2)
    private BigDecimal pensionAmount;


    @Column(name = "medical_insurance_amount", precision = 15, scale = 2)
    private BigDecimal medicalInsuranceAmount;


    @Column(name = "other_tax_amount", precision = 15, scale = 2)
    private BigDecimal otherTaxAmount;


    @Column(name = "gross_salary", precision = 15, scale = 2)
    private BigDecimal grossSalary;


    @Column(name = "net_salary", precision = 15, scale = 2)
    private BigDecimal netSalary;


    @Column(name = "base_salary", precision = 15, scale = 2)
    private BigDecimal baseSalary;


    private Integer month;


    private Integer year;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EPaySlipStatus status = EPaySlipStatus.PENDING;


    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "approved_by")
    private String approvedBy;


    public String getMonthName() {
        String[] months = {"", "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        return months[month];
    }
}

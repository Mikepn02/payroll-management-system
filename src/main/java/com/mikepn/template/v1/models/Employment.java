package com.mikepn.template.v1.models;

import com.mikepn.template.v1.common.AbstractEntity;
import com.mikepn.template.v1.enums.EmployementStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;


@Entity
@Table(name = "employments")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Employment extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;


    @Column(unique = true, nullable = false)
    private String code;

    private String department;
    private String position;
    private BigDecimal baseSalary;

    @Enumerated(EnumType.STRING)
    private EmployementStatus status;

    private LocalDate joiningDate;

    @OneToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;
}

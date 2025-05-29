package com.mikepn.template.v1.models;

import com.mikepn.template.v1.common.AbstractEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;


@Entity
@Table(name = "deductions")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Deduction extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String code;


    @Column(name = "deduction_name", nullable = false)
    private String deductionName;

    @Column(precision = 5, scale = 2)
    private BigDecimal percentage;
}

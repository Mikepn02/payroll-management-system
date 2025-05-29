package com.mikepn.template.v1.enums;

import java.math.BigDecimal;

public enum EDeductionType {
    EMPLOYEE_TAX("Employee Tax", new BigDecimal("30.00")),
    PENSION("Pension", new BigDecimal("6.00")),
    MEDICAL_INSURANCE("Medical Insurance", new BigDecimal("5.00")),
    HOUSING("Housing", new BigDecimal("14.00")),
    TRANSPORT("Transport", new BigDecimal("14.00")),
    OTHERS("Others", new BigDecimal("5.00"));

    private final String name;
    private final BigDecimal defaultPercentage;

    EDeductionType(String name, BigDecimal defaultPercentage) {
        this.name = name;
        this.defaultPercentage = defaultPercentage;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getDefaultPercentage() {
        return defaultPercentage;
    }
}
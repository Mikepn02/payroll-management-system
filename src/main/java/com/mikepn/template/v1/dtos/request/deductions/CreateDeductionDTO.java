package com.mikepn.template.v1.dtos.request.deductions;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateDeductionDTO {

    private String deductionName;
    private BigDecimal percentage;
}

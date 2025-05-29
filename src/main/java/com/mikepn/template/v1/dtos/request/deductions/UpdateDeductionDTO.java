package com.mikepn.template.v1.dtos.request.deductions;

import lombok.Data;

@Data
public class UpdateDeductionDTO {
    private String deductionName;
    private double percentage;
}

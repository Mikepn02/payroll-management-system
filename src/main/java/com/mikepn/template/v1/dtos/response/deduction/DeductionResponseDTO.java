package com.mikepn.template.v1.dtos.response.deduction;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Data
public class DeductionResponseDTO {
    private UUID id;
    private String code;
    private String deductionName;
    private BigDecimal percentage;
}
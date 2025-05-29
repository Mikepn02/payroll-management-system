package com.mikepn.template.v1.services;

import com.mikepn.template.v1.dtos.request.deductions.CreateDeductionDTO;
import com.mikepn.template.v1.dtos.request.deductions.UpdateDeductionDTO;
import com.mikepn.template.v1.models.Deduction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IDeductionService {
    Deduction createDeduction(CreateDeductionDTO deduction);
    List<Deduction> getAllDeductions();
    Deduction getDeductionById(UUID id);
    Deduction getDeductionByCode(String code);
    Deduction updateDeduction(UUID id, UpdateDeductionDTO deduction);
    void deleteDeduction(UUID id);
}

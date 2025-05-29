package com.mikepn.template.v1.services.impl;

import com.mikepn.template.v1.dtos.request.deductions.CreateDeductionDTO;
import com.mikepn.template.v1.dtos.request.deductions.UpdateDeductionDTO;
import com.mikepn.template.v1.exceptions.AppException;
import com.mikepn.template.v1.models.Deduction;
import com.mikepn.template.v1.repositories.IDeductionRepository;
import com.mikepn.template.v1.services.IDeductionService;
import com.mikepn.template.v1.utils.helper.CodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class DeductionServiceImpl implements IDeductionService {
    private final IDeductionRepository deductionRepository;
    private final CodeGenerator codeGenerator;


    @Override
    public Deduction createDeduction(CreateDeductionDTO dto) {
        try{
            Deduction deduction = Deduction.builder()
                    .deductionName(dto.getDeductionName())
                    .percentage(dto.getPercentage())
                    .code(generateUniqueDeductionCode())
                    .build();
            deduction = deductionRepository.save(deduction);
            return deduction;
        } catch (Exception e) {
            throw  new AppException("Deduction Creation Failed: " + e.getMessage());
        }
    }

    @Override
    public List<Deduction> getAllDeductions() {
        return deductionRepository.findAll();
    }

    @Override
    public Deduction getDeductionById(UUID id) {
        return deductionRepository.findById(id)
                .orElseThrow(() -> new AppException("Deduction not found with id: " + id));
    }

    @Override
    public Deduction getDeductionByCode(String code) {
        return deductionRepository.findByCode(code)
                .orElseThrow(() -> new AppException("Deduction not found with code: " + code));
    }

    @Override
    public Deduction updateDeduction(UUID id, UpdateDeductionDTO dto) {
        Deduction deduction = getDeductionById(id);

        try{
            if(dto.getDeductionName() != null){
                deduction.setDeductionName(dto.getDeductionName());
            }

            // Update percentage if provided
            if (dto.getPercentage() != null && dto.getPercentage().compareTo(BigDecimal.ZERO) > 0) {
                deduction.setPercentage(dto.getPercentage());
            }

            deduction = deductionRepository.save(deduction);
            return deduction;

        } catch (Exception e) {
            throw  new AppException("Deduction Update Failed: " + e.getMessage());
        }

    }

    @Override
    public void deleteDeduction(UUID id) {
        Deduction deduction = getDeductionById(id);

        try {
            deductionRepository.delete(deduction);
        } catch (Exception e) {
            throw new AppException("Deduction Deletion Failed: " + e.getMessage());
        }
    }

    public String generateUniqueDeductionCode() {
        String code;
        do {
            code = codeGenerator.generateDeductionCode();
        } while (deductionRepository.existsByCode(code));
        return code;
    }
}

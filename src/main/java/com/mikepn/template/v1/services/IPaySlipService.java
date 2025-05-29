package com.mikepn.template.v1.services;

import com.mikepn.template.v1.dtos.request.PayslipRequestDTO;
import com.mikepn.template.v1.dtos.response.payslip.PayslipResponseDTO;

import java.util.List;
import java.util.UUID;

public interface IPaySlipService {

    List<PayslipResponseDTO> generatePaysSlips(PayslipRequestDTO dto);

    PayslipResponseDTO getPayslipById(UUID id);

    List<PayslipResponseDTO> getPayslipsByEmployeeId(UUID employeeId);

    List<PayslipResponseDTO> getPayslipsByMonthAndYear(Integer month, Integer year);


    PayslipResponseDTO getEmployeePayslipForMonthYear(UUID employeeId, Integer month, Integer year);


    PayslipResponseDTO approvePayslip(UUID payslipId);


    List<PayslipResponseDTO> approveAllPayslips(Integer month, Integer year);

}

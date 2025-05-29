package com.mikepn.template.v1.services.impl;

import com.mikepn.template.v1.dtos.request.PayslipRequestDTO;
import com.mikepn.template.v1.dtos.response.payslip.PayslipResponseDTO;
import com.mikepn.template.v1.enums.EPaySlipStatus;
import com.mikepn.template.v1.enums.EmployementStatus;
import com.mikepn.template.v1.exceptions.AppException;
import com.mikepn.template.v1.models.*;
import com.mikepn.template.v1.repositories.IDeductionRepository;
import com.mikepn.template.v1.repositories.IEmployeeRepository;
import com.mikepn.template.v1.repositories.IEmployementRepository;
import com.mikepn.template.v1.repositories.IPaySlipRepository;
import com.mikepn.template.v1.services.IPaySlipService;
import com.mikepn.template.v1.services.IUserService;
import com.mikepn.template.v1.standalone.EmailService;
import com.mikepn.template.v1.enums.IEmailTemplate;
import com.mikepn.template.v1.utils.helper.PayslipHelper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PayslipServiceImpl implements IPaySlipService {

    private final IPaySlipRepository paySlipRepository;
    private final IEmployeeRepository employeeRepository;
    private final IDeductionRepository deductionRepository;
    private final PayslipHelper payslipHelper;
    private final IUserService userService;
    private final EmailService emailService;

    @Override
    @Transactional
    public List<PayslipResponseDTO> generatePaysSlips(PayslipRequestDTO dto) {
        try {
            List<Employee> activeEmployees = employeeRepository.findAll()
                    .stream()
                    .filter(employee -> employee.getEmployment() != null &&
                            employee.getEmployment().getStatus() == EmployementStatus.ACTIVE)
                    .collect(Collectors.toList());

            List<PayslipResponseDTO> generatedPaysSlips = new ArrayList<>();

            List<Deduction> deductions = deductionRepository.findAll();

            Deduction employeeTax = payslipHelper.findDeductionByName(deductions, "EmployeeTax");
            Deduction pension = payslipHelper.findDeductionByName(deductions, "Pension");
            Deduction medicalInsurance = payslipHelper.findDeductionByName(deductions, "MedicalInsurance");
            Deduction others = payslipHelper.findDeductionByName(deductions, "Others");
            Deduction housing = payslipHelper.findDeductionByName(deductions, "Housing");
            Deduction transport = payslipHelper.findDeductionByName(deductions, "Transport");

            for(Employee employee : activeEmployees){
                Optional<Payslip> existingPayslip = paySlipRepository.findByEmployeeAndMonthAndYear(employee, dto.getMonth(), dto.getYear());

                if (existingPayslip.isPresent()) {
                    continue;
                }

                Employment employment = employee.getEmployment();
                BigDecimal baseSalary = employment.getBaseSalary();

                BigDecimal housingAmount = payslipHelper.calculatePercentage(baseSalary, housing.getPercentage());
                BigDecimal transportAmount = payslipHelper.calculatePercentage(baseSalary, transport.getPercentage());

                BigDecimal grossSalary = baseSalary.add(housingAmount).add(transportAmount);

                BigDecimal employeeTaxAmount = payslipHelper.calculatePercentage(baseSalary, employeeTax.getPercentage());
                BigDecimal pensionAmount = payslipHelper.calculatePercentage(baseSalary, pension.getPercentage());
                BigDecimal medicalInsuranceAmount = payslipHelper.calculatePercentage(baseSalary, medicalInsurance.getPercentage());
                BigDecimal otherTaxAmount = payslipHelper.calculatePercentage(baseSalary, others.getPercentage());

                BigDecimal totalDeductions = employeeTaxAmount.add(pensionAmount).add(medicalInsuranceAmount).add(otherTaxAmount);
                BigDecimal netSalary = grossSalary.subtract(totalDeductions);

                Payslip payslip = Payslip.builder()
                        .employee(employee)
                        .baseSalary(baseSalary)
                        .houseAmount(housingAmount)
                        .transportAmount(transportAmount)
                        .grossSalary(grossSalary)
                        .employeeTaxAmount(employeeTaxAmount)
                        .pensionAmount(pensionAmount)
                        .medicalInsuranceAmount(medicalInsuranceAmount)
                        .otherTaxAmount(otherTaxAmount)
                        .netSalary(netSalary)
                        .month(dto.getMonth())
                        .year(dto.getYear())
                        .status(EPaySlipStatus.PENDING)
                        .build();

                paySlipRepository.save(payslip);
                generatedPaysSlips.add(payslipHelper.mapToPayslipResponseDTO(payslip));
            }

            return generatedPaysSlips;

        } catch (Exception e) {
            throw new AppException("Failed to generate payslips for employees: " + e.getMessage());
        }
    }

    @Override
    public PayslipResponseDTO getPayslipById(UUID id) {
        Payslip payslip = paySlipRepository.findById(id)
                .orElseThrow(() -> new AppException("Payslip not found"));
        return payslipHelper.mapToPayslipResponseDTO(payslip);
    }

    @Override
    public List<PayslipResponseDTO> getPayslipsByEmployeeId(UUID employeeId) {
        List<Payslip> payslips = paySlipRepository.findAllByEmployee_Id(employeeId);
        return payslips.stream()
                .map(payslipHelper::mapToPayslipResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PayslipResponseDTO> getPayslipsByMonthAndYear(Integer month, Integer year) {
        List<Payslip> payslips = paySlipRepository.findAllByMonthAndYear(month, year);
        return payslips.stream()
                .map(payslipHelper::mapToPayslipResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PayslipResponseDTO getEmployeePayslipForMonthYear(UUID employeeId, Integer month, Integer year) {
        Payslip payslip = paySlipRepository.findByEmployee_IdAndMonthAndYear(employeeId, month, year)
                .orElseThrow(() -> new AppException("Payslip not found"));
        return payslipHelper.mapToPayslipResponseDTO(payslip);
    }

    @Override
    @Transactional
    public PayslipResponseDTO approvePayslip(UUID payslipId) {
        User loggedInUser = userService.getLoggedInUser();

        if(loggedInUser == null){
            throw new AppException("User not authenticated");
        }

        Payslip payslip = paySlipRepository.findById(payslipId)
                .orElseThrow(() -> new AppException("Payslip not found"));

        payslip.setStatus(EPaySlipStatus.PAID);
        payslip.setApprovedAt(LocalDateTime.now());
        payslip.setApprovedBy(loggedInUser.getEmail());

        paySlipRepository.save(payslip);

        sendPayslipApprovedEmail(payslip);

        return payslipHelper.mapToPayslipResponseDTO(payslip);
    }

    @Override
    @Transactional
    public List<PayslipResponseDTO> approveAllPayslips(Integer month, Integer year) {
        User user = userService.getLoggedInUser();
        if (user == null) {
            throw new AppException("User not authenticated");
        }

        List<Payslip> pendingPayslips = paySlipRepository.findAllByMonthAndYearAndStatus(month, year, EPaySlipStatus.PENDING);

        List<PayslipResponseDTO> approvedPayslips = new ArrayList<>();

        for (Payslip payslip : pendingPayslips) {
            payslip.setStatus(EPaySlipStatus.PAID);
            payslip.setApprovedAt(LocalDateTime.now());
            payslip.setApprovedBy(user.getEmail());
            paySlipRepository.save(payslip);

            sendPayslipApprovedEmail(payslip);

            approvedPayslips.add(payslipHelper.mapToPayslipResponseDTO(payslip));
        }

        return approvedPayslips;
    }

    private void sendPayslipApprovedEmail(Payslip payslip) {
        String to = payslip.getEmployee().getProfile().getEmail();
        if (to == null || to.isBlank()) {
            System.err.println("Employee email is empty. Skipping notification email.");
            return;
        }

        String username = payslip.getEmployee().getProfile().getFirstName();
        String subject = "Salary Payment Notification";

        Map<String, Object> variables = new HashMap<>();
        variables.put("month", payslip.getMonth());
        variables.put("year", payslip.getYear());
        variables.put("netSalary", payslip.getNetSalary());
        variables.put("institution", "Your Institution");

        try {
            emailService.sendEmail(to, username, subject, IEmailTemplate.SALARY_PAYMENT_NOTIFICATION, variables);
        } catch (Exception e) {
            System.err.println("Failed to send salary notification email: " + e.getMessage());
        }
    }
}

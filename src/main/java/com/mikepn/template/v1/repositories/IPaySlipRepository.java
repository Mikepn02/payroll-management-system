package com.mikepn.template.v1.repositories;

import com.mikepn.template.v1.enums.EPaySlipStatus;
import com.mikepn.template.v1.models.Employee;
import com.mikepn.template.v1.models.Payslip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IPaySlipRepository extends JpaRepository<Payslip , UUID> {

    Optional<Payslip> findByEmployeeAndMonthAndYear(Employee employee, Integer month, Integer year);
    Optional<Payslip> findByEmployee_IdAndMonthAndYear(UUID employeeId, Integer month, Integer year);
    List<Payslip> findAllByMonthAndYear(Integer month, Integer year);

    List<Payslip> findAllByEmployee_Id(UUID employeeId);

    List<Payslip> findAllByEmployeeAndStatus(Employee employee, EPaySlipStatus status);

    List<Payslip> findAllByMonthAndYearAndStatus(Integer month, Integer year, EPaySlipStatus status);


}

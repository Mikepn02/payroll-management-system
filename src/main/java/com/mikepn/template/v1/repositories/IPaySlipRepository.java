package com.mikepn.template.v1.repositories;

import com.mikepn.template.v1.models.Payslip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IPaySlipRepository extends JpaRepository<Payslip , UUID> {
}

package com.mikepn.template.v1.repositories;

import com.mikepn.template.v1.models.Deduction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IDeductionRepository extends JpaRepository<Deduction , UUID> {
    Optional<Deduction> findByCode(String code);
    boolean existsByCode(String code);
}

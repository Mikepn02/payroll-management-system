package com.mikepn.template.v1.repositories;

import com.mikepn.template.v1.models.Deduction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IDeduction extends JpaRepository<Deduction , UUID> {
}

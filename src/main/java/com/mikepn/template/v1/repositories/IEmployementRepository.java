package com.mikepn.template.v1.repositories;

import com.mikepn.template.v1.models.Employment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IEmployementRepository extends JpaRepository<Employment , UUID> {
}

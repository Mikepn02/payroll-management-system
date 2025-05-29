package com.mikepn.template.v1.repositories;

import com.mikepn.template.v1.enums.ERole;
import com.mikepn.template.v1.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IRoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findRoleByName(ERole name);

    boolean existsByName(String name);
}

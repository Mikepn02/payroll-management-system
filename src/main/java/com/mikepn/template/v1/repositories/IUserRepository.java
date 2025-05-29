package com.mikepn.template.v1.repositories;

import com.mikepn.template.v1.models.Role;
import com.mikepn.template.v1.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface IUserRepository extends JpaRepository<User, UUID> {

    Optional<User> findUserByEmail(String email);

    Optional<User> findByRoles(Role role);

    Optional<User> findByVerificationCode(String verificationCode);

    boolean existsByEmail(String email);
}

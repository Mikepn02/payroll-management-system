package com.mikepn.template.v1.utils.helper;

import com.mikepn.template.v1.dtos.request.employee.CreateEmployeeDTO;
import com.mikepn.template.v1.enums.EmployementStatus;
import com.mikepn.template.v1.models.Employee;
import com.mikepn.template.v1.models.Employment;
import com.mikepn.template.v1.models.Role;
import com.mikepn.template.v1.models.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class EmployeeHelper {


    public User buildUserFromDto(CreateEmployeeDTO dto, Role role , PasswordEncoder encoder){
        return User.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .fullName(dto.getFirstName() + " " + dto.getLastName())
                .email(dto.getEmail())
                .dateOfBirth(dto.getDateOfBirth())
                .password(encoder.encode(dto.getPassword()))
                .roles(Set.of(role))
                .build();
    }

    public Employee buildEmployee(User user){
        return Employee.builder()
                .profile(user)
                .build();
    }

    public Employment buildEmployeementFromEmployee(Employee employee, CreateEmployeeDTO dto){
        return  Employment.builder()
                .department(dto.getDepartment())
                .position(dto.getPosition())
                .employee(employee)
                .baseSalary(dto.getBaseSalary())
                .status(EmployementStatus.ACTIVE)
                .joiningDate(dto.getJoiningDate())
                .build();

    }
}

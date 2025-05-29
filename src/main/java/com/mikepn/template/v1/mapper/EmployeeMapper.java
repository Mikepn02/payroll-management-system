package com.mikepn.template.v1.mapper;

import com.mikepn.template.v1.dtos.response.employee.EmployeeResponseDTO;
import com.mikepn.template.v1.models.Employee;
import com.mikepn.template.v1.models.Employment;
import org.springframework.stereotype.Service;

@Service
public class EmployeeMapper {

    public EmployeeResponseDTO toDto(Employee employee, Employment employment) {
        return EmployeeResponseDTO.builder()
                .id(employee.getId())
                .firstName(employee.getProfile().getFirstName())
                .lastName(employee.getProfile().getLastName())
                .email(employee.getProfile().getEmail())
                .dateOfBirth(employee.getProfile().getDateOfBirth())
                .phoneNumber(employee.getProfile().getPhoneNumber())
                .code(employee.getCode())
                .verified(employee.getProfile().isVerified())
                .department(employment.getDepartment())
                .position(employment.getPosition())
                .baseSalary(employment.getBaseSalary())
                .joiningDate(employment.getJoiningDate())
                .employmentStatus(employment.getStatus())
                .build();
    }

    public EmployeeResponseDTO toDto(Employee employee) {
        return EmployeeResponseDTO.builder()
                .id(employee.getId())
                .firstName(employee.getProfile().getFirstName())
                .lastName(employee.getProfile().getLastName())
                .email(employee.getProfile().getEmail())
                .dateOfBirth(employee.getProfile().getDateOfBirth())
                .phoneNumber(employee.getProfile().getPhoneNumber())
                .code(employee.getCode())
                .verified(employee.getProfile().isVerified())
                .build();
    }
}

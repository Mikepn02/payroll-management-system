package com.mikepn.template.v1.services.impl;

import com.mikepn.template.v1.dtos.request.employee.CreateEmployeeDTO;
import com.mikepn.template.v1.dtos.request.employee.UpdateEmployeeDTO;
import com.mikepn.template.v1.dtos.response.employee.EmployeeResponseDTO;
import com.mikepn.template.v1.enums.ERole;
import com.mikepn.template.v1.exceptions.AppException;
import com.mikepn.template.v1.exceptions.BadRequestException;
import com.mikepn.template.v1.exceptions.NotFoundException;
import com.mikepn.template.v1.mapper.EmployeeMapper;
import com.mikepn.template.v1.models.Employee;
import com.mikepn.template.v1.models.Employment;
import com.mikepn.template.v1.models.Role;
import com.mikepn.template.v1.models.User;
import com.mikepn.template.v1.repositories.IEmployeeRepository;
import com.mikepn.template.v1.repositories.IEmployementRepository;
import com.mikepn.template.v1.repositories.IUserRepository;
import com.mikepn.template.v1.services.IEmployementService;
import com.mikepn.template.v1.services.IRoleService;
import com.mikepn.template.v1.utils.helper.CodeGenerator;
import com.mikepn.template.v1.utils.helper.EmployeeHelper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployementServiceImpl implements IEmployementService {

    private final IEmployeeRepository employeeRepository;
    private final IUserRepository userRepository;
    private final IEmployementRepository employementRepository;
    private final IRoleService roleService;
    private final EmployeeHelper employeeHelper;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeMapper employeeMapper;
    private final CodeGenerator codeGenerator;

    @Override
    public EmployeeResponseDTO createEmployee(CreateEmployeeDTO dto) {
        if (employeeRepository.existsByProfile_Email(dto.getEmail())) {
            throw new AppException("An employee with the provided email already exists.");
        }

        try {
            Role role = roleService.getRoleByName(ERole.EMPLOYEE);
            User user = employeeHelper.buildUserFromDto(dto, role, passwordEncoder);
            user = userRepository.save(user);

            Employee employee = employeeHelper.buildEmployee(user);
            employee.setCode(generateUniqueEmployeeCode());
            employee = employeeRepository.save(employee);

            Employment employment = employeeHelper.buildEmployeementFromEmployee(employee, dto);
            employment.setCode(generateUniqueEmployementCode());
            employment = employementRepository.save(employment);

            return employeeMapper.toDto(employee, employment);
        } catch (Exception e) {
            throw new AppException("Employee registration failed: " + e.getMessage());
        }
    }

    @Override
    public EmployeeResponseDTO getEmployeeById(UUID employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("Employee not found with ID: " + employeeId));
        return employeeMapper.toDto(employee);
    }

    @Override
    public void deleteEmployee(UUID employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("Employee not found with ID: " + employeeId));

        try {
            employeeRepository.delete(employee);
        } catch (Exception e) {
            throw new AppException("Failed to delete employee: " + e.getMessage());
        }
    }

    @Override
    public List<EmployeeResponseDTO> getAllEmployees() {
        return employeeRepository.findAll()
                .stream()
                .map(employeeMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public EmployeeResponseDTO updateEmployee(UUID employeeId, UpdateEmployeeDTO dto) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("Employee not found with ID: " + employeeId));

        User userProfile = employee.getProfile();
        if (userProfile == null) {
            throw new BadRequestException("Employee profile is missing.");
        }

        if (dto.getEmail() != null && !userProfile.getEmail().equals(dto.getEmail()) &&
                userRepository.findUserByEmail(dto.getEmail()).isPresent()) {
            throw new BadRequestException("The provided email is already in use.");
        }

        if (dto.getFirstName() != null) userProfile.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) userProfile.setLastName(dto.getLastName());
        if (dto.getEmail() != null) userProfile.setEmail(dto.getEmail());
        if (dto.getPhoneNumber() != null) userProfile.setPhoneNumber(dto.getPhoneNumber());

        String updatedFirstName = dto.getFirstName() != null ? dto.getFirstName() : userProfile.getFirstName();
        String updatedLastName = dto.getLastName() != null ? dto.getLastName() : userProfile.getLastName();
        userProfile.setFullName(updatedFirstName + " " + updatedLastName);

        userRepository.save(userProfile);
        employeeRepository.save(employee);

        return employeeMapper.toDto(employee);
    }

    private String generateUniqueEmployeeCode() {
        String code;
        do {
            code = codeGenerator.generateEmployeeCode();
        } while (employeeRepository.existsByCode(code));
        return code;
    }

    private String generateUniqueEmployementCode() {
        String code;
        do {
            code = codeGenerator.generateEmploymentCode();
        } while (employementRepository.existsByCode(code));
        return code;
    }
}

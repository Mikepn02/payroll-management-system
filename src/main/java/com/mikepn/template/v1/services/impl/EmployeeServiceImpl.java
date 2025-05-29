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
import com.mikepn.template.v1.models.Role;
import com.mikepn.template.v1.models.User;
import com.mikepn.template.v1.repositories.IEmployeeRepository;
import com.mikepn.template.v1.repositories.IUserRepository;
import com.mikepn.template.v1.services.IEmployementService;
import com.mikepn.template.v1.services.IRoleService;
import com.mikepn.template.v1.utils.Mapper;
import com.mikepn.template.v1.utils.helper.EmployeeHelper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements IEmployementService {

    private final IEmployeeRepository employeeRepository;
    private final IUserRepository userRepository;
    private final IRoleService roleService;
    private final EmployeeHelper employeeHelper;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeMapper employeeMapper;

    @Override
    public EmployeeResponseDTO createEmployee(CreateEmployeeDTO dto) {

        if(employeeRepository.existsByProfile_Email(dto.getEmail())){
            throw new AppException("Employee with that email already exists");
        }


        try {

            Role role  = roleService.getRoleByName(ERole.EMPLOYEE);

            User user = employeeHelper.buildUserFromDto(dto, role , passwordEncoder);
            user = userRepository.save(user);


            Employee employee = employeeHelper.buildEmployee(user);
            employee = employeeRepository.save(employee);




            return employeeMapper.toDto(employee);

        } catch (Exception e) {
            throw new AppException("Employee Registration Failed: " + e.getMessage());
        }

    }

    @Override
    public EmployeeResponseDTO getEmployeeById(UUID employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("Employee Not Found"));
        return employeeMapper.toDto(employee);
    }

    @Override
    public void deleteEmployee(UUID employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("Employee Not Found"));
        try{
            employeeRepository.delete(employee);
        } catch (Exception e) {
            throw new AppException("Employee Deletion Failed: " + e.getMessage());
        }
    }

    @Override
    public List<EmployeeResponseDTO> getAllEmployees() {
        return employeeRepository.findAll()
                .stream().map(employee -> employeeMapper.toDto(employee)).toList();
    }

    @Override
    @Transactional
    public EmployeeResponseDTO updateEmployee(UUID employeeId, UpdateEmployeeDTO updateEmployeeDTO) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("Employee not found with id: " + employeeId));

        User userProfile = employee.getProfile();
        if (userProfile == null) {
            throw new BadRequestException("Employee profile not found");
        }

        if (!userProfile.getEmail().equals(updateEmployeeDTO.getEmail()) &&
                userRepository.findUserByEmail(updateEmployeeDTO.getEmail()).isPresent()) {
            throw new BadRequestException("Email is already taken");
        }

        if (updateEmployeeDTO.getFirstName() != null) {
            userProfile.setFirstName(updateEmployeeDTO.getFirstName());
        }

        if (updateEmployeeDTO.getLastName() != null) {
            userProfile.setLastName(updateEmployeeDTO.getLastName());
        }

        if (updateEmployeeDTO.getEmail() != null) {
            if (!userProfile.getEmail().equals(updateEmployeeDTO.getEmail()) &&
                    userRepository.findUserByEmail(updateEmployeeDTO.getEmail()).isPresent()) {
                throw new BadRequestException("Email is already taken");
            }
            userProfile.setEmail(updateEmployeeDTO.getEmail());
        }

        if (updateEmployeeDTO.getPhoneNumber() != null) {
            userProfile.setPhoneNumber(updateEmployeeDTO.getPhoneNumber());
        }

        if (updateEmployeeDTO.getFirstName() != null || updateEmployeeDTO.getLastName() != null) {
            String firstName = updateEmployeeDTO.getFirstName() != null ?
                    updateEmployeeDTO.getFirstName() : userProfile.getFirstName();
            String lastName = updateEmployeeDTO.getLastName() != null ?
                    updateEmployeeDTO.getLastName() : userProfile.getLastName();
            userProfile.setFullName(firstName + " " + lastName);
        }



        userRepository.save(userProfile);
        employeeRepository.save(employee);


        return employeeMapper.toDto(employee);
    }


}

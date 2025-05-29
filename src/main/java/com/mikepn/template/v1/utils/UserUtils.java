package com.mikepn.template.v1.utils;

import com.mikepn.template.v1.models.Employee;
import com.mikepn.template.v1.models.Payslip;
import com.mikepn.template.v1.models.User;
import com.mikepn.template.v1.repositories.IEmployeeRepository;
import com.mikepn.template.v1.repositories.IPaySlipRepository;
import com.mikepn.template.v1.security.user.UserPrincipal;
import com.mikepn.template.v1.services.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserUtils {

    private static final SecureRandom random = new SecureRandom();

    private final IEmployeeRepository employeeRepository;
    private final IPaySlipRepository paySlipRepository;
    private final IUserService userService;

    public static UserPrincipal getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return (UserPrincipal) authentication.getPrincipal();
        }
        return null;
    }

    public static String generateToken() {
        int token = 100000 + random.nextInt(900000);
        return String.valueOf(token);
    }

    public boolean hasRole(String roleName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals(roleName));
    }

    public boolean isPayslipOwner(UUID payslipId) {
        UserPrincipal userPrincipal = getLoggedInUser();
        if (userPrincipal == null) {
            return false;
        }

        Optional<Payslip> payslipOpt = paySlipRepository.findById(payslipId);
        if (payslipOpt.isEmpty()) {
            return false;
        }

        Payslip payslip = payslipOpt.get();
        Employee employee = payslip.getEmployee();

        return employee != null &&
                employee.getProfile() != null &&
                employee.getProfile().getEmail().equals(userPrincipal.getEmail());
    }
    public UUID getCurrentEmployeeId() {
        User user = userService.getLoggedInUser();
        if (user == null) {
            throw new IllegalStateException("No authenticated user found");
        }

        Optional<Employee> employeeOpt = employeeRepository.findByProfile_Email(user.getEmail());
        Employee employee = employeeOpt.orElseThrow(() -> new IllegalStateException("No employee found for the current user"));
        return employee.getId();
    }
}

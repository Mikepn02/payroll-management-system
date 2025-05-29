# ERP Payroll Management System - Complete Implementation

## 1. System Architecture & Design

### Application Architecture
```
Frontend (Optional) → REST API Controllers → Service Layer → Repository Layer → Database
                                          ↓
                            Security Layer (JWT Authentication)
                                          ↓
                              Database Triggers & Messaging
```

### Technology Stack Justification
- **Spring Boot**: Rapid application development with embedded server
- **Spring Data JPA**: Simplified data access layer with automatic repository generation
- **Spring Security + JWT**: Stateless authentication suitable for REST APIs
- **MySQL/PostgreSQL**: Relational database for ACID compliance in financial transactions
- **Swagger**: API documentation and testing interface
- **Maven**: Dependency management and build automation

## 2. Database Schema & ERD

### Entity Relationship Diagram
```
Employee (1) ←→ (1) Employment
    ↓
    (1) ←→ (M) Payslip
    ↓
    (1) ←→ (M) Message

Deductions (M) - Used by → Payroll Calculation Logic
```

### Database Tables

#### Employee Table
```sql
CREATE TABLE employee (
    code VARCHAR(50) PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    roles VARCHAR(100) NOT NULL,
    mobile VARCHAR(20),
    date_of_birth DATE,
    status ENUM('ACTIVE', 'DISABLED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### Employment Table
```sql
CREATE TABLE employment (
    code VARCHAR(50) PRIMARY KEY,
    employee_id VARCHAR(50) NOT NULL,
    department VARCHAR(100) NOT NULL,
    position VARCHAR(100) NOT NULL,
    base_salary DECIMAL(15,2) NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    joining_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employee(code)
);
```

#### Deductions Table
```sql
CREATE TABLE deductions (
    code VARCHAR(50) PRIMARY KEY,
    deduction_name VARCHAR(100) NOT NULL UNIQUE,
    percentage DECIMAL(5,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### Payslip Table
```sql
CREATE TABLE payslip (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_code VARCHAR(50) NOT NULL,
    house_amount DECIMAL(15,2) NOT NULL,
    transport_amount DECIMAL(15,2) NOT NULL,
    employee_taxed_amount DECIMAL(15,2) NOT NULL,
    pension_amount DECIMAL(15,2) NOT NULL,
    medical_insurance_amount DECIMAL(15,2) NOT NULL,
    other_taxed_amount DECIMAL(15,2) NOT NULL,
    gross_salary DECIMAL(15,2) NOT NULL,
    net_salary DECIMAL(15,2) NOT NULL,
    month INT NOT NULL,
    year INT NOT NULL,
    status ENUM('PENDING', 'PAID') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_code) REFERENCES employee(code),
    UNIQUE KEY unique_employee_month_year (employee_code, month, year)
);
```

#### Message Table
```sql
CREATE TABLE message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_code VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    month_year VARCHAR(10) NOT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_code) REFERENCES employee(code)
);
```

## 3. Spring Boot Project Structure

```
src/main/java/com/erp/payroll/
├── ERP PayrollApplication.java
├── config/
│   ├── SecurityConfig.java
│   ├── JwtConfig.java
│   └── SwaggerConfig.java
├── controller/
│   ├── AuthController.java
│   ├── EmployeeController.java
│   ├── EmploymentController.java
│   ├── DeductionController.java
│   ├── PayrollController.java
│   └── MessageController.java
├── dto/
│   ├── LoginRequest.java
│   ├── JwtResponse.java
│   ├── EmployeeDto.java
│   ├── PayslipDto.java
│   └── PayrollRequest.java
├── entity/
│   ├── Employee.java
│   ├── Employment.java
│   ├── Deduction.java
│   ├── Payslip.java
│   └── Message.java
├── repository/
│   ├── EmployeeRepository.java
│   ├── EmploymentRepository.java
│   ├── DeductionRepository.java
│   ├── PayslipRepository.java
│   └── MessageRepository.java
├── service/
│   ├── AuthService.java
│   ├── EmployeeService.java
│   ├── PayrollService.java
│   ├── EmailService.java
│   └── DeductionService.java
├── security/
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   └── CustomUserDetailsService.java
└── exception/
    ├── GlobalExceptionHandler.java
    └── CustomExceptions.java
```

## 4. Key POJOs/Entities

### Employee Entity
```java
@Entity
@Table(name = "employee")
public class Employee {
    @Id
    private String code;
    
    @Column(name = "first_name", nullable = false)
    private String firstName;
    
    @Column(name = "last_name", nullable = false)
    private String lastName;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    private Role roles;
    
    private String mobile;
    
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;
    
    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;
    
    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL)
    private Employment employment;
    
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private List<Payslip> payslips;
    
    // Constructors, getters, setters
}

enum Role {
    ROLE_ADMIN, ROLE_MANAGER, ROLE_EMPLOYEE
}

enum Status {
    ACTIVE, DISABLED
}
```

### Payslip Entity
```java
@Entity
@Table(name = "payslip")
public class Payslip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_code")
    private Employee employee;
    
    @Column(name = "house_amount", precision = 15, scale = 2)
    private BigDecimal houseAmount;
    
    @Column(name = "transport_amount", precision = 15, scale = 2)
    private BigDecimal transportAmount;
    
    @Column(name = "employee_taxed_amount", precision = 15, scale = 2)
    private BigDecimal employeeTaxedAmount;
    
    @Column(name = "pension_amount", precision = 15, scale = 2)
    private BigDecimal pensionAmount;
    
    @Column(name = "medical_insurance_amount", precision = 15, scale = 2)
    private BigDecimal medicalInsuranceAmount;
    
    @Column(name = "other_taxed_amount", precision = 15, scale = 2)
    private BigDecimal otherTaxedAmount;
    
    @Column(name = "gross_salary", precision = 15, scale = 2)
    private BigDecimal grossSalary;
    
    @Column(name = "net_salary", precision = 15, scale = 2)
    private BigDecimal netSalary;
    
    private Integer month;
    private Integer year;
    
    @Enumerated(EnumType.STRING)
    private PayslipStatus status = PayslipStatus.PENDING;
    
    // Constructors, getters, setters
}

enum PayslipStatus {
    PENDING, PAID
}
```

## 5. Core Service Implementation

### PayrollService
```java
@Service
@Transactional
public class PayrollService {
    
    @Autowired
    private EmploymentRepository employmentRepository;
    
    @Autowired
    private DeductionRepository deductionRepository;
    
    @Autowired
    private PayslipRepository payslipRepository;
    
    public List<Payslip> generatePayroll(int month, int year) {
        // Get all active employments
        List<Employment> activeEmployments = employmentRepository.findByStatus(EmploymentStatus.ACTIVE);
        
        // Get current deduction rates
        Map<String, BigDecimal> deductions = getDeductionRates();
        
        List<Payslip> payslips = new ArrayList<>();
        
        for (Employment employment : activeEmployments) {
            // Check if payslip already exists for this month/year
            if (payslipRepository.existsByEmployeeCodeAndMonthAndYear(
                    employment.getEmployee().getCode(), month, year)) {
                throw new PayrollAlreadyGeneratedException(
                    "Payroll already generated for employee: " + employment.getEmployee().getCode());
            }
            
            Payslip payslip = calculatePayslip(employment, deductions, month, year);
            payslips.add(payslip);
        }
        
        return payslipRepository.saveAll(payslips);
    }
    
    private Payslip calculatePayslip(Employment employment, Map<String, BigDecimal> deductions, 
                                   int month, int year) {
        BigDecimal baseSalary = employment.getBaseSalary();
        
        // Calculate amounts
        BigDecimal housingAmount = baseSalary.multiply(deductions.get("Housing")).divide(BigDecimal.valueOf(100));
        BigDecimal transportAmount = baseSalary.multiply(deductions.get("Transport")).divide(BigDecimal.valueOf(100));
        BigDecimal grossSalary = baseSalary.add(housingAmount).add(transportAmount);
        
        BigDecimal employeeTax = baseSalary.multiply(deductions.get("Employee Tax")).divide(BigDecimal.valueOf(100));
        BigDecimal pension = baseSalary.multiply(deductions.get("Pension")).divide(BigDecimal.valueOf(100));
        BigDecimal medicalInsurance = baseSalary.multiply(deductions.get("Medical Insurance")).divide(BigDecimal.valueOf(100));
        BigDecimal others = baseSalary.multiply(deductions.get("Others")).divide(BigDecimal.valueOf(100));
        
        BigDecimal totalDeductions = employeeTax.add(pension).add(medicalInsurance).add(others);
        BigDecimal netSalary = grossSalary.subtract(totalDeductions);
        
        // Ensure deductions don't exceed gross salary
        if (totalDeductions.compareTo(grossSalary) > 0) {
            throw new IllegalArgumentException("Total deductions exceed gross salary for employee: " + 
                employment.getEmployee().getCode());
        }
        
        Payslip payslip = new Payslip();
        payslip.setEmployee(employment.getEmployee());
        payslip.setHouseAmount(housingAmount);
        payslip.setTransportAmount(transportAmount);
        payslip.setEmployeeTaxedAmount(employeeTax);
        payslip.setPensionAmount(pension);
        payslip.setMedicalInsuranceAmount(medicalInsurance);
        payslip.setOtherTaxedAmount(others);
        payslip.setGrossSalary(grossSalary);
        payslip.setNetSalary(netSalary);
        payslip.setMonth(month);
        payslip.setYear(year);
        payslip.setStatus(PayslipStatus.PENDING);
        
        return payslip;
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public void approvePayroll(int month, int year) {
        List<Payslip> pendingPayslips = payslipRepository.findByMonthAndYearAndStatus(
            month, year, PayslipStatus.PENDING);
        
        for (Payslip payslip : pendingPayslips) {
            payslip.setStatus(PayslipStatus.PAID);
        }
        
        payslipRepository.saveAll(pendingPayslips);
        // Database trigger will handle message generation
    }
}
```

## 6. API Endpoints

### Authentication Controller
```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest loginRequest) {
        // Authentication logic
    }
    
    @PostMapping("/register")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<String> register(@RequestBody EmployeeDto employeeDto) {
        // Employee registration logic
    }
}
```

### Payroll Controller
```java
@RestController
@RequestMapping("/api/payroll")
public class PayrollController {
    
    @PostMapping("/generate")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<PayslipDto>> generatePayroll(@RequestBody PayrollRequest request) {
        // Generate payroll for given month/year
    }
    
    @PutMapping("/approve/{month}/{year}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> approvePayroll(@PathVariable int month, @PathVariable int year) {
        // Approve payroll
    }
    
    @GetMapping("/payslip/{month}/{year}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<PayslipDto> getMyPayslip(@PathVariable int month, @PathVariable int year) {
        // Get employee's own payslip
    }
    
    @GetMapping("/payslips/{month}/{year}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<PayslipDto>> getAllPayslips(@PathVariable int month, @PathVariable int year) {
        // Get all payslips for the month/year
    }
}
```

## 7. Database Trigger for Messaging

```sql
DELIMITER $$

CREATE TRIGGER payslip_approval_trigger
    AFTER UPDATE ON payslip
    FOR EACH ROW
BEGIN
    IF NEW.status = 'PAID' AND OLD.status = 'PENDING' THEN
        INSERT INTO message (employee_code, message, month_year)
        SELECT 
            NEW.employee_code,
            CONCAT('Dear ', e.first_name, ', your salary for ', NEW.month, '/', NEW.year, 
                   ' from Rwanda Government amounting to ', NEW.net_salary, 
                   ' has been credited to your account ', NEW.employee_code, ' successfully.'),
            CONCAT(NEW.month, '-', NEW.year)
        FROM employee e 
        WHERE e.code = NEW.employee_code;
    END IF;
END$$

DELIMITER ;
```

## 8. JWT Security Configuration

```java
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint() {
        return new JwtAuthenticationEntryPoint();
    }
    
    @Bean
    public JwtRequestFilter jwtRequestFilter() {
        return new JwtRequestFilter();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
            
        http.addFilterBefore(jwtRequestFilter(), UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

## 9. Application Properties

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/erp_payroll
spring.datasource.username=root
spring.datasource.password=password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# JWT Configuration
jwt.secret=mySecretKey
jwt.expiration=86400

# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Swagger Configuration
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
```

## 10. Maven Dependencies

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-mail</artifactId>
    </dependency>
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.11.5</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.11.5</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.11.5</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.0.2</version>
    </dependency>
</dependencies>
```

## 11. Sample Data Insertion

```sql
-- Insert default deductions
INSERT INTO deductions (code, deduction_name, percentage) VALUES
('EMP_TAX', 'Employee Tax', 30.00),
('PENSION', 'Pension', 6.00),
('MED_INS', 'Medical Insurance', 5.00),
('HOUSING', 'Housing', 14.00),
('TRANSPORT', 'Transport', 14.00),
('OTHERS', 'Others', 5.00);

-- Insert sample employees
INSERT INTO employee (code, first_name, last_name, email, password, roles, mobile, date_of_birth, status) VALUES
('EMP001', 'John', 'Doe', 'admin@example.com', '$2a$10$encrypted_password', 'ROLE_ADMIN', '0788123456', '1985-01-15', 'ACTIVE'),
('EMP002', 'Jane', 'Smith', 'manager@example.com', '$2a$10$encrypted_password', 'ROLE_MANAGER', '0788654321', '1990-05-20', 'ACTIVE'),
('EMP003', 'David', 'Mugabo', 'david@example.com', '$2a$10$encrypted_password', 'ROLE_EMPLOYEE', '0788987654', '1992-08-10', 'ACTIVE');

-- Insert employments
INSERT INTO employment (code, employee_id, department, position, base_salary, status, joining_date) VALUES
('EMP001_EMP', 'EMP001', 'IT', 'System Administrator', 100000.00, 'ACTIVE', '2023-01-01'),
('EMP002_EMP', 'EMP002', 'HR', 'HR Manager', 80000.00, 'ACTIVE', '2023-02-01'),
('EMP003_EMP', 'EMP003', 'Finance', 'Accountant', 70000.00, 'ACTIVE', '2023-03-01');
```

## 12. Testing & Documentation

### Swagger UI Access
- URL: `http://localhost:8080/swagger-ui.html`
- Provides interactive API documentation and testing interface

### API Testing Workflow
1. **Authentication**: POST `/api/auth/login` with email/password
2. **Add JWT Token**: Use returned token in Authorization header: `Bearer <token>`
3. **Generate Payroll**: POST `/api/payroll/generate` (MANAGER role)
4. **Approve Payroll**: PUT `/api/payroll/approve/{month}/{year}` (ADMIN role)
5. **View Payslips**: GET `/api/payroll/payslip/{month}/{year}` (EMPLOYEE role)

## 13. Spring Boot Flow Diagram

```
Client Request → Security Filter Chain → JWT Authentication Filter
                                              ↓
JWT Token Validation → User Details Service → Authorization Check
                                              ↓
Controller → Service Layer → Repository Layer → Database
     ↑                                              ↓
Response ← Business Logic ← Data Access ← Query Execution
                              ↓
                    Database Triggers (Message Generation)
                              ↓
                         Email Service
```

This comprehensive implementation provides a secure, scalable ERP payroll management system with all required features including role-based access control, automated payroll calculation, and messaging integration.
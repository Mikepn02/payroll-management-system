package com.mikepn.template.v1.dtos.request.employee;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;


@Data
public class UpdateEmployeeDTO {

    @NotBlank(message = "First name cannot be blank")
    @Pattern(regexp = "^[A-Za-z].*", message = "First name must start with a letter")
    private String firstName;


    @Schema(example = "Doe")
    @NotBlank(message = "Last name cannot be blank")
    private String lastName;

    @Schema(example = "example@gmail.com")
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    private String email;

    @Schema(example = "0788671061")
    @NotBlank(message = "Phone number cannot be blank")
    private String phoneNumber;

    @Schema(example = "1990-01-01")
    @NotBlank(message = "Date of birth cannot be blank")
    private LocalDate dateOfBirth;
}

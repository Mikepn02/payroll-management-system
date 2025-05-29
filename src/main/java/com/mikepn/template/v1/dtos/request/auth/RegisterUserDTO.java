package com.mikepn.template.v1.dtos.request.auth;


import com.mikepn.template.v1.annotations.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;


@Data
public class RegisterUserDTO {


    @NotBlank(message = "First name cannot be blank")
    @Pattern(regexp = "^[A-Za-z].*", message = "First name must start with a letter")
    @Schema(example = "John")
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

    @NotNull(message = "Date of birth cannot be null")
    @Schema(example = "1990-01-01")
    private LocalDate dateOfBirth;

    @Schema(example = "password@123")
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @ValidPassword(message = "Password should be strong")
    private String password;
}

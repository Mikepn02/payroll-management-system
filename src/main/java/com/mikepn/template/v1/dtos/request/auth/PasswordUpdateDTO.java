package com.mikepn.template.v1.dtos.request.auth;


import com.mikepn.template.v1.annotations.ValidPassword;
import lombok.Data;

@Data
public class PasswordUpdateDTO {
    private String oldPassword;
    @ValidPassword(message = "Password should be strong")
    private String newPassword;
}
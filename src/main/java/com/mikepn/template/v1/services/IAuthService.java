package com.mikepn.template.v1.services;

import com.mikepn.template.v1.dtos.request.auth.LoginDTO;
import com.mikepn.template.v1.dtos.request.auth.RegisterUserDTO;
import com.mikepn.template.v1.dtos.request.user.UserResponseDTO;
import com.mikepn.template.v1.dtos.response.auth.AuthResponse;

public interface IAuthService {

    AuthResponse login(LoginDTO signInDTO);

    void forgotPassword(String email);



    void resetPassword(String email, String passwordResetCode, String newPassword);

    void initiateAccountVerificaton(String email);

    void verifyAccount(String verificationCode);

    void resendVerificationCode(String email);

    void updatePassword(String email, String oldPassword, String newPassword);
}

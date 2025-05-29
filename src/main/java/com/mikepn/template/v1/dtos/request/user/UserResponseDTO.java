package com.mikepn.template.v1.dtos.request.user;


import com.mikepn.template.v1.models.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponseDTO {
    private User user;
}
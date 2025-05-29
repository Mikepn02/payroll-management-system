package com.mikepn.template.v1.dtos.response.role;


import com.mikepn.template.v1.models.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class RoleResponseDTO {
    private Role role;
}
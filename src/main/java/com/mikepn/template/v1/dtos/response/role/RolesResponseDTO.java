package com.mikepn.template.v1.dtos.response.role;


import com.mikepn.template.v1.models.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

@Data
@AllArgsConstructor
@Builder
public class RolesResponseDTO {
    private Page<Role> roles;
}
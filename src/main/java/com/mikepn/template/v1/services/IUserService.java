package com.mikepn.template.v1.services;

import com.mikepn.template.v1.dtos.request.auth.RegisterUserDTO;
import com.mikepn.template.v1.dtos.request.auth.UpdateUserDTO;
import com.mikepn.template.v1.dtos.request.user.CreateAdminDTO;
import com.mikepn.template.v1.dtos.request.user.UserResponseDTO;
import com.mikepn.template.v1.dtos.request.user.UserRoleModificationDTO;
import com.mikepn.template.v1.models.User;

import java.util.List;
import java.util.UUID;

public interface IUserService {

    User findUserById(UUID userId);

    User getLoggedInUser();

    UserResponseDTO createAdmin(CreateAdminDTO createUserDTO);
    UserResponseDTO createManager(RegisterUserDTO userDTO);

    List<User> getUsers();

    UserResponseDTO getUserById(UUID uuid);

    UserResponseDTO updateUser(UUID userId, UpdateUserDTO updateUserDTO);

    UserResponseDTO addRoles(UUID userId, UserRoleModificationDTO userRoleModificationDTO);

    UserResponseDTO removeRoles(UUID userId, UserRoleModificationDTO userRoleModificationDTO);

    void deleteUser(UUID userId);
}

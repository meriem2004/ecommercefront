package com.example.service;

import com.example.dto.UserDTO;

import java.util.List;
import java.util.Optional;

public interface UserService {
    UserDTO createUser(UserDTO userDTO);
    UserDTO updateUser(Long id, UserDTO userDTO);
    void deleteUser(Long id);
    Optional<UserDTO> getUserById(Long id);
    Optional<UserDTO> getUserByEmail(String email);
    List<UserDTO> getAllUsers();
    UserDTO assignRolesToUser(Long userId, List<String> roleNames);
    boolean authenticateUser(String email, String password);
    boolean changePassword(String email, String currentPassword, String newPassword);
}
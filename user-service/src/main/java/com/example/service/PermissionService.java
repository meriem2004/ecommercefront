package com.example.service;

import com.example.dto.PermissionDTO;
import java.util.List;

public interface PermissionService {
    List<PermissionDTO> getAllPermissions();
    PermissionDTO createPermission(PermissionDTO permissionDTO);
    PermissionDTO updatePermission(Long id, PermissionDTO permissionDTO);
    void deletePermission(Long id);
} 
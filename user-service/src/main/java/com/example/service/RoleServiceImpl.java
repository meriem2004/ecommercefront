package com.example.service;

import com.example.dto.RoleDTO;
import com.example.exception.ResourceNotFoundException;
import com.example.model.Permission;
import com.example.model.Role;
import com.example.repository.PermissionRepository;
import com.example.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Autowired
    public RoleServiceImpl(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public List<RoleDTO> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RoleDTO createRole(RoleDTO roleDTO) {
        // Check if role name already exists
        if (roleRepository.existsByName(roleDTO.getName())) {
            throw new IllegalArgumentException("Role name already exists: " + roleDTO.getName());
        }

        Role role = convertToEntity(roleDTO);
        Role savedRole = roleRepository.save(role);
        return convertToDTO(savedRole);
    }

    @Override
    @Transactional
    public RoleDTO updateRole(Long id, RoleDTO roleDTO) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));

        // Check if the new name conflicts with existing roles (excluding current one)
        if (!role.getName().equals(roleDTO.getName()) && 
            roleRepository.existsByName(roleDTO.getName())) {
            throw new IllegalArgumentException("Role name already exists: " + roleDTO.getName());
        }

        role.setName(roleDTO.getName());
        role.setDescription(roleDTO.getDescription());

        // Update permissions if provided
        if (roleDTO.getPermissionNames() != null) {
            Set<Permission> permissions = new HashSet<>();
            for (String permissionName : roleDTO.getPermissionNames()) {
                Permission permission = permissionRepository.findByName(permissionName)
                        .orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + permissionName));
                permissions.add(permission);
            }
            role.setPermissions(permissions);
        }

        Role updatedRole = roleRepository.save(role);
        return convertToDTO(updatedRole);
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        
        roleRepository.delete(role);
    }

    private Role convertToEntity(RoleDTO roleDTO) {
        Role role = new Role();
        role.setName(roleDTO.getName());
        role.setDescription(roleDTO.getDescription());

        // Convert permission names to Permission entities
        if (roleDTO.getPermissionNames() != null && !roleDTO.getPermissionNames().isEmpty()) {
            Set<Permission> permissions = new HashSet<>();
            for (String permissionName : roleDTO.getPermissionNames()) {
                Permission permission = permissionRepository.findByName(permissionName)
                        .orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + permissionName));
                permissions.add(permission);
            }
            role.setPermissions(permissions);
        }

        return role;
    }

    private RoleDTO convertToDTO(Role role) {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(role.getId());
        roleDTO.setName(role.getName());
        roleDTO.setDescription(role.getDescription());

        // Convert permissions to permission names
        if (role.getPermissions() != null && !role.getPermissions().isEmpty()) {
            List<String> permissionNames = role.getPermissions().stream()
                    .map(Permission::getName)
                    .collect(Collectors.toList());
            roleDTO.setPermissionNames(permissionNames);
        }

        return roleDTO;
    }
}
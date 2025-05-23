package com.example.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

@Data
public class RoleDTO {
    private Long id;

    @NotBlank(message = "Role name is required")
    private String name;

    private String description;
    private Set<String> permissions;
} 
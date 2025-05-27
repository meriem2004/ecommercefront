package com.example.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PermissionDTO {
    private Long id;

    @NotBlank(message = "Permission name is required")
    private String name;

    private String description;
} 
package com.example;

import com.example.model.Permission;
import com.example.model.Role;
import com.example.repository.PermissionRepository;
import com.example.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SpringBootApplication
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner init(PermissionRepository permissionRepository, RoleRepository roleRepository) {
        return args -> {
            // Only initialize if no permissions exist (prevents duplicate data)
            if (permissionRepository.count() == 0) {
                System.out.println("Initializing permissions and roles...");
                
                // Create permissions
                List<Permission> permissions = Arrays.asList(
                    new Permission(null, "READ_PRODUCTS", "Can read products"),
                    new Permission(null, "WRITE_PRODUCTS", "Can create and edit products"),
                    new Permission(null, "DELETE_PRODUCTS", "Can delete products"),
                    new Permission(null, "READ_USERS", "Can read users"),
                    new Permission(null, "WRITE_USERS", "Can create and edit users"),
                    new Permission(null, "DELETE_USERS", "Can delete users"),
                    new Permission(null, "READ_ORDERS", "Can read orders"),
                    new Permission(null, "WRITE_ORDERS", "Can create and edit orders")
                );
                
                permissionRepository.saveAll(permissions);
                System.out.println("Permissions created successfully");

                // Create roles with permissions
                createRolesWithPermissions(roleRepository, permissionRepository);
                System.out.println("Roles created successfully");
            } else {
                System.out.println("Data already exists, skipping initialization");
            }
        };
    }

    private void createRolesWithPermissions(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        // ADMIN role - all permissions
        if (!roleRepository.existsByName("ROLE_ADMIN")) {
            Role adminRole = new Role();
            adminRole.setName("ROLE_ADMIN");
            adminRole.setDescription("Administrator with full access");
            
            Set<Permission> adminPermissions = new HashSet<>();
            adminPermissions.add(permissionRepository.findByName("READ_PRODUCTS").orElseThrow());
            adminPermissions.add(permissionRepository.findByName("WRITE_PRODUCTS").orElseThrow());
            adminPermissions.add(permissionRepository.findByName("DELETE_PRODUCTS").orElseThrow());
            adminPermissions.add(permissionRepository.findByName("READ_USERS").orElseThrow());
            adminPermissions.add(permissionRepository.findByName("WRITE_USERS").orElseThrow());
            adminPermissions.add(permissionRepository.findByName("DELETE_USERS").orElseThrow());
            adminPermissions.add(permissionRepository.findByName("READ_ORDERS").orElseThrow());
            adminPermissions.add(permissionRepository.findByName("WRITE_ORDERS").orElseThrow());
            
            adminRole.setPermissions(adminPermissions);
            roleRepository.save(adminRole);
        }

        // MANAGER role - read/write access, no delete users
        if (!roleRepository.existsByName("ROLE_MANAGER")) {
            Role managerRole = new Role();
            managerRole.setName("ROLE_MANAGER");
            managerRole.setDescription("Manager with read and write access");
            
            Set<Permission> managerPermissions = new HashSet<>();
            managerPermissions.add(permissionRepository.findByName("READ_PRODUCTS").orElseThrow());
            managerPermissions.add(permissionRepository.findByName("WRITE_PRODUCTS").orElseThrow());
            managerPermissions.add(permissionRepository.findByName("DELETE_PRODUCTS").orElseThrow());
            managerPermissions.add(permissionRepository.findByName("READ_USERS").orElseThrow());
            managerPermissions.add(permissionRepository.findByName("READ_ORDERS").orElseThrow());
            managerPermissions.add(permissionRepository.findByName("WRITE_ORDERS").orElseThrow());
            
            managerRole.setPermissions(managerPermissions);
            roleRepository.save(managerRole);
        }

        // CUSTOMER role - basic read access
        if (!roleRepository.existsByName("ROLE_CUSTOMER")) {
            Role customerRole = new Role();
            customerRole.setName("ROLE_CUSTOMER");
            customerRole.setDescription("Customer with basic access");
            
            Set<Permission> customerPermissions = new HashSet<>();
            customerPermissions.add(permissionRepository.findByName("READ_PRODUCTS").orElseThrow());
            customerPermissions.add(permissionRepository.findByName("READ_ORDERS").orElseThrow());
            
            customerRole.setPermissions(customerPermissions);
            roleRepository.save(customerRole);
        }
    }
}
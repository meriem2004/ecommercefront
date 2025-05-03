package com.example;

import com.example.model.Permission;
import com.example.model.Role;
import com.example.repository.PermissionRepository;
import com.example.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@SpringBootApplication
@EnableDiscoveryClient
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        return args -> {
            // Create permissions
            Permission readProducts = new Permission(null, "READ_PRODUCTS", "Can view products");
            Permission writeProducts = new Permission(null, "WRITE_PRODUCTS", "Can create and update products");
            Permission deleteProducts = new Permission(null, "DELETE_PRODUCTS", "Can delete products");
            Permission readOrders = new Permission(null, "READ_ORDERS", "Can view orders");
            Permission writeOrders = new Permission(null, "WRITE_ORDERS", "Can update orders");
            Permission manageUsers = new Permission(null, "MANAGE_USERS", "Can manage users");
            Permission placeOrders = new Permission(null, "PLACE_ORDERS", "Can place orders");
            Permission supportAccess = new Permission(null, "SUPPORT_ACCESS", "Access to customer support functions");

            List<Permission> allPermissions = Arrays.asList(
                    readProducts, writeProducts, deleteProducts, readOrders,
                    writeOrders, manageUsers, placeOrders, supportAccess
            );

            permissionRepository.saveAll(allPermissions);

            // Create roles
            Role customerRole = new Role();
            customerRole.setName("ROLE_CUSTOMER");
            customerRole.setDescription("Regular customer");
            customerRole.setPermissions(new HashSet<>(Arrays.asList(readProducts, placeOrders)));

            Role adminRole = new Role();
            adminRole.setName("ROLE_ADMIN");
            adminRole.setDescription("Administrator with full access");
            adminRole.setPermissions(new HashSet<>(allPermissions));

            Role guestRole = new Role();
            guestRole.setName("ROLE_GUEST");
            guestRole.setDescription("Unauthenticated guest user");
            guestRole.setPermissions(new HashSet<>(Arrays.asList(readProducts)));

            roleRepository.saveAll(Arrays.asList(customerRole, adminRole, guestRole));
        };
    }
}
package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan({"com.example", "com.example.config", "com.example.filter"}) 
public class ApiGatewayApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
    
    // Fallback route to ensure gateway has at least one route for testing
    @Bean
    public RouteLocator fallbackRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("fallback", r -> r
                .path("/fallback")
                .uri("http://localhost:" + 8080 + "/actuator/health"))
            .build();
    }
}
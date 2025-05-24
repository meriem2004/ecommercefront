package com.example.config;

import com.example.filter.AuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.core.annotation.Order;

@Configuration
public class RouteConfig {

    @Autowired
    private AuthenticationFilter authFilter;

    @Bean
    @Order(-1)
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // OPTIONS requests (CORS preflight) - HIGHEST PRIORITY
                .route("options-preflight", r -> r
                        .path("/api/**")
                        .and()
                        .method(HttpMethod.OPTIONS)
                        .uri("lb://cart-service"))
                
                // Auth routes (PUBLIC) - NO AUTH FILTER
                .route("auth-routes", r -> r
                        .path("/api/auth/**")
                        .uri("lb://user-service"))
                
                // SPECIFIC CART ROUTES FIRST (more specific paths must come before general ones)
                
                // Debug endpoint - PUBLIC but with optional auth to set headers
                .route("cart-debug", r -> r
                        .path("/api/carts/debug-auth")
                        .filters(f -> f.filter(authFilter.apply(new AuthenticationFilter.Config(false))))
                        .uri("lb://cart-service"))
                
                // Cart current - Optional auth (will set headers if token present)
                .route("cart-current", r -> r
                        .path("/api/carts/current")
                        .and()
                        .method(HttpMethod.GET)
                        .filters(f -> f.filter(authFilter.apply(new AuthenticationFilter.Config(false))))
                        .uri("lb://cart-service"))
                
                // Cart operations - PROTECTED (auth required)
                .route("cart-operations", r -> r
                        .path("/api/carts/current/items/**", "/api/carts/current/items")
                        .and()
                        .method(HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.PATCH)
                        .filters(f -> f.filter(authFilter.apply(new AuthenticationFilter.Config(true))))
                        .uri("lb://cart-service"))
                
                // General cart routes - Optional auth
                .route("cart-general", r -> r
                        .path("/api/carts/**")
                        .filters(f -> f.filter(authFilter.apply(new AuthenticationFilter.Config(false))))
                        .uri("lb://cart-service"))
                
                // User routes - Auth required
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .filters(f -> f.filter(authFilter.apply(new AuthenticationFilter.Config(true))))
                        .uri("lb://user-service"))
                
                // Gateway debug routes
                .route("gateway-debug", r -> r
                        .path("/api/gateway/**")
                        .filters(f -> f.filter(authFilter.apply(new AuthenticationFilter.Config(false))))
                        .uri("lb://cart-service"))
                
                .build();
    }
}
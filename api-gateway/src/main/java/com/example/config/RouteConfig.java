package com.example.config;

import com.example.filter.AuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Configuration
public class RouteConfig {

    @Autowired
    private AuthenticationFilter authFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth routes - NO FILTERS (handled by service itself)
                 .route("auth-routes", r -> r
                        .path("/api/auth/**")
                        .uri("lb://user-service"))
                
                // Debug endpoints - all public, NO FILTERS
                .route("debug-endpoints", r -> r
                        .path("/api/debug/**")
                        .uri("lb://product-service"))
                
                // Public product routes - NO FILTERS
                .route("public-products-get", r -> r
                        .path("/api/products/**")
                        .and()
                        .method(HttpMethod.GET)
                        .uri("lb://product-service"))
                
                // Public category routes - NO FILTERS
                .route("public-categories-get", r -> r
                        .path("/api/categories/**")
                        .and()
                        .method(HttpMethod.GET)
                        .uri("lb://product-service"))
                
                // Protected routes
                .route("user-service-protected", r -> r
                        .path("/api/users/**")
                        .filters(f -> f.filter(authFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://user-service"))
                
                // Protected product routes (write operations)
                .route("product-service-protected", r -> r
                        .path("/api/products/**")
                        .and()
                        .method(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH, HttpMethod.DELETE)
                        .filters(f -> f.filter(authFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://product-service"))
                
                // Protected category routes (write operations)
                .route("category-service-protected", r -> r
                        .path("/api/categories/**")
                        .and()
                        .method(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH, HttpMethod.DELETE)
                        .filters(f -> f.filter(authFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://product-service"))
                
                // Cart service routes
                .route("cart-service", r -> r
                        .path("/api/carts/**")
                        .filters(f -> f.filter(authFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://cart-service"))
                
                // Order service routes
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .filters(f -> f.filter(authFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://order-service"))
                .build();
    }
}
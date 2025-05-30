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
                // CORS preflight - HIGHEST PRIORITY
                .route("cors-preflight", r -> r
                        .method(HttpMethod.OPTIONS)
                        .and()
                        .path("/**")
                        .uri("lb://cart-service"))
                
                // Auth routes - NO FILTERS
                .route("auth-routes", r -> r
                        .path("/api/auth/**")
                        .uri("lb://user-service"))
                
                // Debug endpoints - NO FILTERS
                .route("debug-endpoints", r -> r
                        .path("/api/debug/**")
                        .uri("lb://product-service"))
                
                // Product routes
                .route("all-products-no-auth", r -> r
                        .path("/api/products/**")
                        .uri("lb://product-service"))
                
                // Category routes
                .route("all-categories-no-auth", r -> r
                        .path("/api/categories/**")
                        .uri("lb://product-service"))
                
                // Cart routes
                .route("all-cart-no-auth", r -> r
                        .path("/api/carts/**")
                        .uri("lb://cart-service"))
                
                // Order routes
                .route("all-orders-no-auth", r -> r
                        .path("/api/orders/**")
                        .uri("lb://order-service"))
                
                // Payment routes - Try different approaches
                .route("payment-service-direct", r -> r
                        .path("/api/payments/**")
                        .uri("http://localhost:8085"))
                
                // Fallback payment route with load balancer
                .route("payment-service-lb", r -> r
                        .path("/api/payments-lb/**")
                        .filters(f -> f.rewritePath("/api/payments-lb/(?<segment>.*)", "/api/payments/${segment}"))
                        .uri("lb://payment-service"))
                
                // Protected user routes
                .route("user-service-protected", r -> r
                        .path("/api/users/**")
                        .filters(f -> f.filter(authFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://user-service"))
                
                .build();
    }
}
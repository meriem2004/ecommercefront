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
                        .uri("lb://cart-service")) // Route OPTIONS to any service
                
                // Auth routes - NO FILTERS
                .route("auth-routes", r -> r
                        .path("/api/auth/**")
                        .uri("lb://user-service"))
                
                // Debug endpoints - NO FILTERS
                .route("debug-endpoints", r -> r
                        .path("/api/debug/**")
                        .uri("lb://product-service"))
                
                // ✅ ALL PRODUCT ROUTES - NO AUTHENTICATION (TEMPORARILY)
                .route("all-products-no-auth", r -> r
                        .path("/api/products/**")
                        .uri("lb://product-service"))
                
                // ✅ ALL CATEGORY ROUTES - NO AUTHENTICATION (TEMPORARILY)  
                .route("all-categories-no-auth", r -> r
                        .path("/api/categories/**")
                        .uri("lb://product-service"))
                
                // ✅ ALL CART ROUTES - NO AUTHENTICATION (TEMPORARILY)
                .route("all-cart-no-auth", r -> r
                        .path("/api/carts/**")
                        .uri("lb://cart-service"))
                
                // Protected user routes
                .route("user-service-protected", r -> r
                        .path("/api/users/**")
                        .filters(f -> f.filter(authFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://user-service"))
                
                // Order service routes
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .filters(f -> f.filter(authFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://order-service"))
                .build();
    }
}
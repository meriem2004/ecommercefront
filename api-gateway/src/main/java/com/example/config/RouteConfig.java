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
                // Handle OPTIONS requests first
                .route("options-preflight", r -> r
                        .path("/**")
                        .and()
                        .method(HttpMethod.OPTIONS)
                        .uri("no://op"))
                
                // Public product routes
                .route("public-products", r -> r
                        .path("/api/products/**")
                        .and()
                        .method(HttpMethod.GET)
                        .uri("lb://product-service"))
                
                // Public category routes
                .route("public-categories", r -> r
                        .path("/api/categories/**")
                        .and()
                        .method(HttpMethod.GET)
                        .uri("lb://product-service"))
                
                // Auth routes
                .route("auth-routes", r -> r
                        .path("/api/auth/**")
                        .uri("lb://user-service"))
                
                // Protected routes
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .filters(f -> f.filter(authFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://user-service"))
                
                .route("product-service-protected", r -> r
                        .path("/api/products/**")
                        .filters(f -> f.filter(authFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://product-service"))
                
                .route("cart-service", r -> r
                        .path("/api/carts/**")
                        .filters(f -> f.filter(authFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://cart-service"))
                
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .filters(f -> f.filter(authFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://order-service"))
                .build();
    }
}
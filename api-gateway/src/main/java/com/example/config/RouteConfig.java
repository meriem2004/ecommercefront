package com.example.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

public class RouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder){
        return builder.routes()
                .route("product-service-route", r -> r
                        .path("/api/product/**")
                        .uri("lb://product-service"))
                .route("user-service-route", r -> r
                        .path("/api/users/**")
                        .uri("lb://user-service"))
                .route("cart-service-route", r -> r
                        .path("/api/carts/**")
                        .uri("lb://cart-service"))
                .route("order-service-route", r -> r
                        .path("/api/orders/**")
                        .uri("lb://order-service"))
                .route("payment-service-route", r -> r
                        .path("/api/payments/**")
                        .uri("lb://payment-service"))
                .route("shipping-service-route", r -> r
                        .path("/api/shipping/**")
                        .uri("lb://shipping-service"))
                .route("notification-service-route", r -> r
                        .path("/api/notifications/**")
                        .uri("lb://notification-service"))
                .build();
    }
}

package com.example.config;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
public class SimpleCorsConfig implements GlobalFilter, Ordered {

    private static final List<String> ALLOWED_ORIGINS = Arrays.asList(
        "http://localhost:5173",  // Vite dev server
        "http://127.0.0.1:5173",
        "http://localhost:3000",  // In case you use React's default port
        "http://127.0.0.1:3000"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        HttpHeaders headers = response.getHeaders();

        String origin = request.getHeaders().getOrigin();
        
        // Set CORS headers for all responses, regardless of whether they've been set already
        // This ensures they're applied consistently
        if (origin != null && (ALLOWED_ORIGINS.contains(origin) || origin.startsWith("http://localhost:"))) {
            headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
            headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE, OPTIONS, PATCH");
            headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Authorization, Content-Type, X-Requested-With, Accept, X-User-Email, X-User-Roles");
            headers.set(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Authorization, X-User-Email, X-User-Id");
            headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
            headers.set(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");
            
            // IMPORTANT: Remove WWW-Authenticate header for all responses
            headers.remove(HttpHeaders.WWW_AUTHENTICATE);
        }

        // Handle preflight requests
        if (request.getMethod() == HttpMethod.OPTIONS) {
            response.setStatusCode(HttpStatus.OK);
            return Mono.empty();
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // Ensure this runs first to set the headers correctly
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
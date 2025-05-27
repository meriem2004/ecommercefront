package com.example.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(SimpleCorsConfig.class);
    
    private static final List<String> ALLOWED_ORIGINS = Arrays.asList(
        "http://localhost:5173",
        "http://127.0.0.1:5173"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        
        String origin = request.getHeaders().getOrigin();
        String path = request.getURI().getPath();
        String method = request.getMethod() != null ? request.getMethod().name() : "UNKNOWN";
        
        logger.debug("CORS Filter - Method: {}, Path: {}, Origin: {}", method, path, origin);

        // Always add CORS headers for allowed origins
        if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
            addCorsHeaders(response, origin);
            logger.debug("Added CORS headers for origin: {}", origin);
        }

        // Handle OPTIONS (preflight) requests immediately
        if (HttpMethod.OPTIONS.equals(request.getMethod())) {
            logger.info("Handling OPTIONS preflight request for path: {}", path);
            response.setStatusCode(HttpStatus.OK);
            response.getHeaders().setContentLength(0);
            return response.setComplete();
        }

        return chain.filter(exchange);
    }

    private void addCorsHeaders(ServerHttpResponse response, String origin) {
        HttpHeaders headers = response.getHeaders();
        
        headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
        headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE, OPTIONS, PATCH");
        headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, 
            "Authorization, Content-Type, X-Requested-With, Accept, X-User-Email, X-User-Roles, X-User-Id");
        headers.set(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, 
            "Authorization, X-User-Email, X-User-Id, X-User-Roles");
        headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        headers.set(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
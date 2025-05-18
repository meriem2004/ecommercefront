package com.example.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().toString();
        String headers = formatHeaders(request.getHeaders());
        String query = request.getURI().getQuery() != null ? request.getURI().getQuery() : "";

        LOGGER.info("Incoming Request: {} {} Query: {} Headers: {}", 
                method, path, query, headers);

        long startTime = System.currentTimeMillis();
        
        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    long duration = System.currentTimeMillis() - startTime;
                    LOGGER.info("Response: {} {} completed in {}ms with status {}",
                            method,
                            path,
                            duration,
                            exchange.getResponse().getStatusCode());
                    
                    // Log response headers for debugging
                    LOGGER.debug("Response Headers: {}", 
                            formatHeaders(exchange.getResponse().getHeaders()));
                }));
    }

    private String formatHeaders(HttpHeaders headers) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        headers.forEach((key, values) -> {
            sb.append(key).append(": ");
            if (key.equalsIgnoreCase("authorization")) {
                // Mask sensitive info but show token type for debugging
                List<String> authHeaders = headers.get(key);
                if (authHeaders != null && !authHeaders.isEmpty()) {
                    String auth = authHeaders.get(0);
                    if (auth.startsWith("Bearer ")) {
                        sb.append("Bearer ***");
                    } else {
                        sb.append("*****");
                    }
                } else {
                    sb.append("*****");
                }
            } else {
                sb.append(String.join(", ", values));
            }
            sb.append(", ");
        });
        if (sb.length() > 1) {
            sb.setLength(sb.length() - 2);
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public int getOrder() {
        // Execute this filter before other filters
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
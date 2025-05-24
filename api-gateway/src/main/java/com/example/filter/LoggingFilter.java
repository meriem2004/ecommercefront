package com.example.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
    private static final String CORRELATION_ID = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Generate or use existing correlation ID
        String correlationIdHeader = request.getHeaders().getFirst(CORRELATION_ID);
        final String correlationId = (correlationIdHeader == null || correlationIdHeader.isEmpty())
                ? UUID.randomUUID().toString()
                : correlationIdHeader;

        // Add correlation ID to request
        ServerHttpRequest modifiedRequest = request.mutate()
                .header(CORRELATION_ID, correlationId)
                .build();

        // Log request details
        logger.info("Incoming request - ID: {}, Method: {}, Path: {}, Headers: {}",
                correlationId,
                request.getMethod(),
                request.getURI().getPath(),
                maskSensitiveHeaders(request.getHeaders()));

        long startTime = System.currentTimeMillis();

        return chain.filter(exchange.mutate().request(modifiedRequest).build())
                .doFinally(signalType -> {
                    ServerHttpResponse response = exchange.getResponse();
                    long duration = System.currentTimeMillis() - startTime;
                    
                    logger.info("Outgoing response - ID: {}, Status: {}, Duration: {}ms",
                            correlationId,
                            response.getStatusCode(),
                            duration);
                });
    }

    private String maskSensitiveHeaders(HttpHeaders headers) {
        StringBuilder sb = new StringBuilder("{");
        headers.forEach((name, values) -> {
            sb.append(name).append(": ");
            if (name.equalsIgnoreCase(HttpHeaders.AUTHORIZATION)) {
                sb.append(values.stream().map(v -> v.startsWith("Bearer ") ? "Bearer ***" : "***")
                        .findFirst().orElse(""));
            } else {
                sb.append(String.join(", ", values));
            }
            sb.append("; ");
        });
        sb.append("}");
        return sb.toString();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
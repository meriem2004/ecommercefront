package com.example.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.List;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    @Value("${jwt.secret}")
    private String secret;

    private static final List<String> PUBLIC_GET_ENDPOINTS = Arrays.asList(
        "/api/products",
        "/api/categories",
        "/api/debug"
    );

    private static final List<String> EXCLUDED_AUTH_ENDPOINTS = Arrays.asList(
        "/api/auth/login",
        "/api/auth/register",
        "/api/auth/refresh"
    );

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();
            HttpMethod method = request.getMethod();

            logger.debug("Processing request: {} {}", method, path);

            // Allow OPTIONS requests (CORS preflight)
            if (method == HttpMethod.OPTIONS) {
                return chain.filter(exchange);
            }

            // Check if path matches any public GET endpoints
            if (method == HttpMethod.GET && isPublicPath(path)) {
                return chain.filter(exchange);
            }

            // Check excluded authentication paths
            if (isExcludedPath(path)) {
                return chain.filter(exchange);
            }

            // All other paths require JWT authentication
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "Missing Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Invalid Authorization header format", HttpStatus.UNAUTHORIZED);
            }

            try {
                String token = authHeader.substring(7);
                Claims claims = validateToken(token);
                
                // Add user info to headers for downstream services
                ServerHttpRequest modifiedRequest = enhanceRequestWithUserInfo(request, claims);
                return chain.filter(exchange.mutate().request(modifiedRequest).build());
                
            } catch (ExpiredJwtException e) {
                return onError(exchange, "Token expired", HttpStatus.UNAUTHORIZED);
            } catch (UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
                return onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_GET_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    private boolean isExcludedPath(String path) {
        return EXCLUDED_AUTH_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    private Claims validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private ServerHttpRequest enhanceRequestWithUserInfo(ServerHttpRequest request, Claims claims) {
        String username = claims.getSubject();
        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);

        return request.mutate()
                .header("X-User-Id", claims.getId())
                .header("X-User-Email", username)
                .header("X-User-Roles", roles != null ? String.join(",", roles) : "")
                .build();
    }

    private Mono<Void> onError(ServerWebExchange exchange, String error, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        String errorJson = String.format("{\"error\":\"%s\"}", error);
        return response.writeWith(Mono.just(
            response.bufferFactory().wrap(errorJson.getBytes(StandardCharsets.UTF_8))
        ));
    }

    public static class Config {
        // Configuration properties if needed
    }
}
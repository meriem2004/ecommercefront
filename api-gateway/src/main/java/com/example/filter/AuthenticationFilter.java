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

            logger.info("Processing request: {} {}", method, path);

            // Allow OPTIONS requests (CORS preflight) - IMMEDIATELY
            if (method == HttpMethod.OPTIONS) {
                logger.info("Allowing OPTIONS request for CORS preflight");
                return chain.filter(exchange);
            }

            // Check if path matches any public GET endpoints
            if (method == HttpMethod.GET && isPublicPath(path)) {
                logger.info("Allowing public GET request to: {}", path);
                return chain.filter(exchange);
            }

            // Check excluded authentication paths
            if (isExcludedPath(path)) {
                logger.info("Allowing excluded auth path: {}", path);
                return chain.filter(exchange);
            }

            // Log all headers for debugging
            logger.info("Request headers: {}", request.getHeaders().toSingleValueMap());

            // All other paths require JWT authentication
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                logger.error("Missing Authorization header for protected endpoint: {} {}", method, path);
                return onError(exchange, "Missing Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.error("Invalid Authorization header format: {}", authHeader);
                return onError(exchange, "Invalid Authorization header format", HttpStatus.UNAUTHORIZED);
            }

            try {
                String token = authHeader.substring(7);
                logger.info("Validating JWT token: {}", token.substring(0, Math.min(20, token.length())) + "...");
                
                Claims claims = validateToken(token);
                logger.info("Token validated successfully for user: {}", claims.getSubject());
                
                // Add user info to headers for downstream services
                ServerHttpRequest modifiedRequest = enhanceRequestWithUserInfo(request, claims);
                return chain.filter(exchange.mutate().request(modifiedRequest).build());
                
            } catch (ExpiredJwtException e) {
                logger.error("Token expired: {}", e.getMessage());
                return onError(exchange, "Token expired", HttpStatus.UNAUTHORIZED);
            } catch (UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
                logger.error("Invalid token: {}", e.getMessage());
                return onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
            } catch (Exception e) {
                logger.error("Unexpected error during token validation: {}", e.getMessage(), e);
                return onError(exchange, "Authentication error", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private boolean isPublicPath(String path) {
        boolean isPublic = PUBLIC_GET_ENDPOINTS.stream().anyMatch(path::startsWith);
        logger.debug("Path {} is public: {}", path, isPublic);
        return isPublic;
    }

    private boolean isExcludedPath(String path) {
        boolean isExcluded = EXCLUDED_AUTH_ENDPOINTS.stream().anyMatch(path::startsWith);
        logger.debug("Path {} is excluded from auth: {}", path, isExcluded);
        return isExcluded;
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

        logger.info("Adding user headers - User: {}, Roles: {}", username, roles);

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
        
        // Add CORS headers to error responses
        response.getHeaders().add("Access-Control-Allow-Origin", "http://localhost:5173");
        response.getHeaders().add("Access-Control-Allow-Credentials", "true");
        response.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.getHeaders().add("Access-Control-Allow-Headers", "*");
        
        String errorJson = String.format("{\"error\":\"%s\", \"status\":%d, \"timestamp\":\"%s\"}", 
            error, status.value(), java.time.Instant.now().toString());
        
        logger.error("Returning error response: {}", errorJson);
        
        return response.writeWith(Mono.just(
            response.bufferFactory().wrap(errorJson.getBytes(StandardCharsets.UTF_8))
        ));
    }

    public static class Config {
        // Configuration properties if needed
    }
}
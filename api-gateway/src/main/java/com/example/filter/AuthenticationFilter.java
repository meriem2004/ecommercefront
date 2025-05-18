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
import java.util.ArrayList;
import java.util.List;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    @Value("${jwt.secret}")
    private String secret;

    private final List<String> excludedUrls = List.of(
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
            String method = request.getMethod().toString();
            
            logger.debug("Processing request: {} {}", method, path);
            
            // Always allow OPTIONS requests (CORS preflight)
            if (request.getMethod() == HttpMethod.OPTIONS) {
                logger.debug("Allowing OPTIONS request");
                return chain.filter(exchange);
            }
            
            // Always allow GET requests to product and category endpoints
            if (request.getMethod() == HttpMethod.GET && 
                (path.startsWith("/api/products") || path.startsWith("/api/categories"))) {
                logger.debug("Allowing public GET endpoint: {}", path);
                return chain.filter(exchange);
            }
            
            // Check excluded authentication paths
            for (String excludedUrl : excludedUrls) {
                if (path.startsWith(excludedUrl)) {
                    logger.debug("Allowing excluded path: {}", path);
                    return chain.filter(exchange);
                }
            }
            
            // All other paths require JWT authentication
            logger.debug("Authentication required for: {} {}", method, path);
            
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                logger.debug("Missing Authorization header for: {}", path);
                return onError(exchange, "No Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.debug("Invalid Authorization header format: {}", authHeader);
                return onError(exchange, "Invalid Authorization header format", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);
            
            try {
                Claims claims = validateToken(token);
                logger.debug("Token validated for user: {}", claims.getSubject());
                
                // Add user info to headers for downstream services
                ServerHttpRequest modifiedRequest = addUserInfoToHeaders(request, claims);
                return chain.filter(exchange.mutate().request(modifiedRequest).build());
                
            } catch (ExpiredJwtException e) {
                logger.debug("Token expired: {}", e.getMessage());
                return onError(exchange, "Token expired", HttpStatus.UNAUTHORIZED);
            } catch (UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
                logger.debug("Token validation failed: {}", e.getMessage());
                return onError(exchange, "Invalid token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
            }
        };
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

    private ServerHttpRequest addUserInfoToHeaders(ServerHttpRequest request, Claims claims) {
        String username = claims.getSubject();
        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);
    
        // Forward the original Authorization header for downstream services
        String originalAuthHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    
        return request.mutate()
                .header(HttpHeaders.AUTHORIZATION, originalAuthHeader) // Add this line to forward the Authorization header
                .header("X-User-Email", username)
                .header("X-User-Roles", roles != null ? String.join(",", roles) : "")
                .build();
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        
        String errorJson = "{\"error\":\"" + message + "\"}";
        byte[] bytes = errorJson.getBytes(StandardCharsets.UTF_8);
        
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    public static class Config {
        // Configuration properties if needed
    }
}
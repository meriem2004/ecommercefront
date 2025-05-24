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
        "/api/debug",
        "/api/carts/debug-auth"
    );

    private static final List<String> EXCLUDED_AUTH_ENDPOINTS = Arrays.asList(
        "/api/auth/login",
        "/api/auth/register",
        "/api/auth/refresh",
        "/api/auth/test"
        
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

            logger.info("🔍 PROCESSING REQUEST: {} {} - Auth required: {}", method, path, config.isRequired());

            // Allow OPTIONS requests (CORS preflight)
            if (method == HttpMethod.OPTIONS) {
                logger.debug("✅ OPTIONS request - allowing");
                return chain.filter(exchange);
            }

            // Check if path matches any public GET endpoints
            if (method == HttpMethod.GET && isPublicPath(path)) {
                logger.debug("✅ Public GET endpoint - allowing access");
                return chain.filter(exchange);
            }

            // Check excluded authentication paths
            if (isExcludedPath(path)) {
                logger.debug("✅ Excluded auth endpoint - allowing access");
                return chain.filter(exchange);
            }

            // Check for Authorization header
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                logger.warn("⚠️ Missing Authorization header for: {}", path);
                if (config.isRequired()) {
                    logger.error("❌ Auth required but no header - returning 401");
                    return onError(exchange, "Missing Authorization header", HttpStatus.UNAUTHORIZED);
                } else {
                    logger.debug("✅ Optional auth - no header provided, continuing");
                    return chain.filter(exchange);
                }
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            logger.debug("🔑 Auth header received: {}", authHeader != null ? authHeader.substring(0, Math.min(20, authHeader.length())) + "..." : "null");
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("⚠️ Invalid Authorization header format for: {}", path);
                if (config.isRequired()) {
                    logger.error("❌ Auth required but invalid header format - returning 401");
                    return onError(exchange, "Invalid Authorization header format", HttpStatus.UNAUTHORIZED);
                } else {
                    logger.debug("✅ Optional auth - invalid header format, continuing");
                    return chain.filter(exchange);
                }
            }

            try {
                String token = authHeader.substring(7);
                logger.debug("🎫 Attempting to validate token: {}...", token.substring(0, Math.min(20, token.length())));
                
                Claims claims = validateToken(token);
                
                logger.info("✅ Token validated successfully!");
                logger.info("📋 Claims - Subject: {}, UserId: {}, Roles: {}", 
                           claims.getSubject(), 
                           claims.get("userId"), 
                           claims.get("roles"));
                
                // Add user info to headers for downstream services
                ServerHttpRequest modifiedRequest = enhanceRequestWithUserInfo(request, claims);
                return chain.filter(exchange.mutate().request(modifiedRequest).build());
                
            } catch (ExpiredJwtException e) {
                logger.error("❌ Token expired for path: {} - Error: {}", path, e.getMessage());
                if (config.isRequired()) {
                    return onError(exchange, "Token expired", HttpStatus.UNAUTHORIZED);
                } else {
                    return chain.filter(exchange);
                }
            } catch (UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
                logger.error("❌ Invalid token for path: {} - Error: {}", path, e.getMessage());
                logger.error("🔧 Token validation error details:", e);
                if (config.isRequired()) {
                    return onError(exchange, "Invalid token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
                } else {
                    return chain.filter(exchange);
                }
            } catch (Exception e) {
                logger.error("❌ Unexpected error validating token for path: {} - Error: {}", path, e.getMessage());
                logger.error("🔧 Unexpected error details:", e);
                if (config.isRequired()) {
                    return onError(exchange, "Token validation failed: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
                } else {
                    return chain.filter(exchange);
                }
            }
        };
    }

    private boolean isPublicPath(String path) {
        boolean isPublic = PUBLIC_GET_ENDPOINTS.stream().anyMatch(path::startsWith);
        logger.debug("🔍 Is public path '{}': {}", path, isPublic);
        return isPublic;
    }

    private boolean isExcludedPath(String path) {
        boolean isExcluded = EXCLUDED_AUTH_ENDPOINTS.stream().anyMatch(path::startsWith);
        logger.debug("🔍 Is excluded path '{}': {}", path, isExcluded);
        return isExcluded;
    }

    private Claims validateToken(String token) {
        logger.debug("🔧 Validating token with secret (first 10 chars): {}...", secret.substring(0, Math.min(10, secret.length())));
        
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            logger.debug("✅ Token parsed successfully. Claims: {}", claims);
            return claims;
        } catch (Exception e) {
            logger.error("❌ Token validation failed with error: {}", e.getMessage());
            throw e;
        }
    }

    private Key getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        Key key = Keys.hmacShaKeyFor(keyBytes);
        logger.debug("🔑 Generated signing key from secret");
        return key;
    }

    private ServerHttpRequest enhanceRequestWithUserInfo(ServerHttpRequest request, Claims claims) {
        String username = claims.getSubject(); // This should be the email
        String userId = claims.get("userId", String.class);
        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);

        logger.info("🔄 Enhancing request with user info:");
        logger.info("   📧 Email: {}", username);
        logger.info("   🆔 UserId: {}", userId);
        logger.info("   🎭 Roles: {}", roles);

        ServerHttpRequest.Builder builder = request.mutate()
                .header("X-User-Email", username != null ? username : "")
                .header("Authorization", request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION)); // Forward original token
        
        if (userId != null) {
            builder.header("X-User-Id", userId);
        }
        
        if (roles != null && !roles.isEmpty()) {
            builder.header("X-User-Roles", String.join(",", roles));
        }

        ServerHttpRequest modifiedRequest = builder.build();
        
        logger.info("✅ Request enhanced with headers:");
        logger.info("   X-User-Email: {}", modifiedRequest.getHeaders().getFirst("X-User-Email"));
        logger.info("   X-User-Id: {}", modifiedRequest.getHeaders().getFirst("X-User-Id"));
        logger.info("   X-User-Roles: {}", modifiedRequest.getHeaders().getFirst("X-User-Roles"));
        
        return modifiedRequest;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String error, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        // IMPORTANT: Remove WWW-Authenticate header to prevent basic auth popup
        response.getHeaders().remove(HttpHeaders.WWW_AUTHENTICATE);
        
        logger.error("🚫 Returning error response: {} - {}", status, error);
        
        String errorJson = String.format("{\"error\":\"%s\",\"message\":\"Authentication required\"}", error);
        return response.writeWith(Mono.just(
            response.bufferFactory().wrap(errorJson.getBytes(StandardCharsets.UTF_8))
        ));
    }

    public static class Config {
        private boolean required = true;

        public Config() {
            this(true);
        }
        
        public Config(boolean required) {
            this.required = required;
        }
        
        public boolean isRequired() {
            return required;
        }
        
        public void setRequired(boolean required) {
            this.required = required;
        }
    }
}
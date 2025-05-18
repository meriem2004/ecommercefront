/*
package com.example.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;
import java.util.stream.Collectors;

// DISABLED - Using AuthenticationFilter instead
// @Component
public class JwtAuthenticationWebFilter implements WebFilter {

    @Value("${jwt.secret}")
    private String secret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Skip filter if no Authorization header
        if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            return chain.filter(exchange);
        }
        
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }
        
        String token = authHeader.substring(7);
        
        try {
            Claims claims = validateToken(token);
            String username = claims.getSubject();
            
            @SuppressWarnings("unchecked")
            List<String> roles = claims.get("roles", List.class);
            
            List<SimpleGrantedAuthority> authorities = roles != null ? 
                roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()) : 
                List.of();
            
            UsernamePasswordAuthenticationToken auth = 
                new UsernamePasswordAuthenticationToken(username, null, authorities);
            
            // Forward auth headers to downstream services
            ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Email", username)
                .header("X-User-Roles", roles != null ? String.join(",", roles) : "")
                .build();
            
            ServerWebExchange modifiedExchange = exchange.mutate().request(modifiedRequest).build();
            
            return chain.filter(modifiedExchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
                
        } catch (Exception e) {
            // Continue chain without setting authentication
            return chain.filter(exchange);
        }
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
}
*/
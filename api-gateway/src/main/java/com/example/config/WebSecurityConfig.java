package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(csrf -> csrf.disable())
            // Explicitly disable CORS here since we handle it in SimpleCorsConfig
            .cors(cors -> cors.disable())
            .authorizeExchange(exchanges -> exchanges
                // Public auth endpoints - MUST match exactly
                .pathMatchers("/api/auth/login", "/api/auth/register", "/api/auth/refresh").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/auth/**").permitAll() // For test endpoint
                
                // Public product endpoints
                .pathMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                
                // Allow all OPTIONS requests (CORS preflight)
                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // All other requests require authentication
                .anyExchange().authenticated()
            )
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable())
        .build();
    }
}
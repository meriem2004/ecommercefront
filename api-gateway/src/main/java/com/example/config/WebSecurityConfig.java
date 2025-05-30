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
            .cors().and() // Enable CORS
            .authorizeExchange(exchanges -> exchanges
                // Allow ALL OPTIONS requests - HIGHEST PRIORITY (CORS preflight)
                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // Actuator endpoints
                .pathMatchers("/actuator/**").permitAll()
                
                // Public auth endpoints
                .pathMatchers("/api/auth/**").permitAll()
                
                // Public product and category GET endpoints
                .pathMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/debug/**").permitAll()
                
                // Debug: Allow cart endpoints temporarily for testing
                .pathMatchers("/api/carts/**").permitAll()
                
                // All other requests need authentication (handled by AuthenticationFilter)
                .anyExchange().permitAll() // Let the AuthenticationFilter handle auth
            )
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable())
            .build();
    }
}
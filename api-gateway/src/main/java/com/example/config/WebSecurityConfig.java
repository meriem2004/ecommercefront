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
            .authorizeExchange(exchanges -> exchanges
                // Allow ALL OPTIONS requests - HIGHEST PRIORITY (before any other rules)
                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // Actuator endpoints
                .pathMatchers("/actuator/**").permitAll()
                
                // Public auth endpoints
                .pathMatchers("/api/auth/**").permitAll()
                
                // Public product endpoints
                .pathMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/debug/**").permitAll()
                
                // All other requests require authentication
                .anyExchange().authenticated()
            )
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable())
            .build();
    }
}
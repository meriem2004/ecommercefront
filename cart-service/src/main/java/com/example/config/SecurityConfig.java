package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            
            // Set session management to stateless
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
                
            // Allow all requests since Gateway handles authentication
            .authorizeHttpRequests(auth -> auth
                // Allow OPTIONS requests for CORS
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Allow actuator endpoints
                .requestMatchers("/actuator/**").permitAll()
                // Allow all cart endpoints - Gateway already authenticated the request
                .requestMatchers("/api/carts/**").permitAll()
                // Allow all other requests (Gateway handles auth)
                .anyRequest().permitAll()
            );

        return http.build();
    }
}
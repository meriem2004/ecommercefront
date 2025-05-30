package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsGlobalConfiguration {
    
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        
        // Allow specific origins
        corsConfig.addAllowedOrigin("http://localhost:5173");
        corsConfig.addAllowedOriginPattern("http://localhost:*");
        
        // Allow all HTTP methods
        corsConfig.addAllowedMethod("*");
        
        // Allow all headers
        corsConfig.addAllowedHeader("*");
        
        // Allow credentials
        corsConfig.setAllowCredentials(true);
        
        // Cache preflight response for 1 hour
        corsConfig.setMaxAge(3600L);
        
        // Expose these headers to the client
        corsConfig.addExposedHeader("Authorization");
        corsConfig.addExposedHeader("X-User-Email");
        corsConfig.addExposedHeader("X-User-Id");
        corsConfig.addExposedHeader("X-User-Roles");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
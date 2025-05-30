package com.example.security;

import com.example.config.JwtTokenUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    // Public endpoints that don't require JWT authentication
    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
        "/actuator/"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        String method = request.getMethod();

        logger.debug("Processing request: {} {}", method, requestPath);

        // Skip JWT processing for OPTIONS requests (CORS preflight)
        if ("OPTIONS".equalsIgnoreCase(method)) {
            logger.debug("Skipping JWT processing for OPTIONS request");
            chain.doFilter(request, response);
            return;
        }

        // Skip JWT processing for public endpoints
        if (isPublicEndpoint(requestPath)) {
            logger.debug("Skipping JWT processing for public endpoint: {}", requestPath);
            chain.doFilter(request, response);
            return;
        }

        final String requestTokenHeader = request.getHeader("Authorization");
        String username = null;
        String jwtToken = null;

        // JWT Token is in the form "Bearer token". Remove Bearer word and get only the Token
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtTokenUtil.extractUsername(jwtToken);
                logger.debug("Extracted username from JWT: {}", username);
            } catch (Exception e) {
                logger.warn("JWT Token validation failed: {}", e.getMessage());
            }
        } else {
            logger.debug("JWT Token does not begin with Bearer String or is null");
        }

        // Once we get the token, validate it and set authentication
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // if token is valid configure Spring Security to manually set authentication
                if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = 
                        new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken
                            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                    logger.debug("Authentication set for user: {}", username);
                } else {
                    logger.warn("JWT Token validation failed for user: {}", username);
                }
            } catch (Exception e) {
                logger.error("Error setting authentication for user: {} - {}", username, e.getMessage());
            }
        }
        
        chain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String requestPath) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(requestPath::startsWith);
    }
}
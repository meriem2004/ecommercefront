package com.example.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @GetMapping("/cors")
    public ResponseEntity<Map<String, Object>> debugCors(
            @RequestHeader(value = "Origin", required = false) String origin,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "CORS debug endpoint");
        response.put("timestamp", System.currentTimeMillis());
        response.put("service", "product-service");
        
        // Add request info
        Map<String, Object> requestInfo = new HashMap<>();
        requestInfo.put("origin", origin);
        requestInfo.put("hasAuthorization", authorization != null && !authorization.isEmpty());
        
        response.put("request", requestInfo);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "product-service");
        status.put("status", "UP");
        status.put("timestamp", System.currentTimeMillis());
        
        // Add some basic system info
        status.put("javaVersion", System.getProperty("java.version"));
        status.put("memory", Runtime.getRuntime().freeMemory() + "/" + Runtime.getRuntime().totalMemory());
        
        // Add authentication info if available
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            Map<String, Object> authInfo = new HashMap<>();
            authInfo.put("principal", auth.getPrincipal().toString());
            authInfo.put("isAuthenticated", auth.isAuthenticated());
            authInfo.put("authorities", auth.getAuthorities());
            
            status.put("authentication", authInfo);
        }
        
        return ResponseEntity.ok(status);
    }
    
    @GetMapping("/public-test")
    public ResponseEntity<Map<String, Object>> publicTest() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "This is a public endpoint that should be accessible without authentication");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/secured-test")
    public ResponseEntity<Map<String, Object>> securedTest() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "This is a secured endpoint that requires authentication");
        response.put("timestamp", System.currentTimeMillis());
        
        // Add authentication info
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        response.put("principal", auth.getPrincipal());
        response.put("authorities", auth.getAuthorities());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/security-info")
    public ResponseEntity<Map<String, Object>> getSecurityInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("timestamp", System.currentTimeMillis());
        
        try {
            // Get security context
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                info.put("authenticated", auth.isAuthenticated());
                info.put("principal", auth.getPrincipal().toString());
                info.put("authorities", auth.getAuthorities().toString());
                info.put("details", auth.getDetails() != null ? auth.getDetails().toString() : null);
            } else {
                info.put("authenticated", false);
                info.put("message", "No authentication found in security context");
            }
            
            // Add Spring Security context info
            info.put("securityContextHolderStrategy", SecurityContextHolder.getContextHolderStrategy().getClass().getName());
            
        } catch (Exception e) {
            info.put("error", e.getMessage());
            info.put("stackTrace", e.getStackTrace()[0].toString());
        }
        
        return ResponseEntity.ok(info);
    }
}
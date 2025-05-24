package com.example.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.client.UserServiceClient;
import com.example.dto.*;
import com.example.service.CartService;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true", 
    allowedHeaders = {"Authorization", "Content-Type"})
public class CartController {
    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartService cartService;
    
    @Autowired
    private UserServiceClient userServiceClient;

    // Helper method to get user ID from Gateway headers (PRIORITY)
    private Long getUserIdFromHeaders(HttpServletRequest request) {
        String userIdHeader = request.getHeader("X-User-Id");
        if (userIdHeader != null && !userIdHeader.isEmpty()) {
            try {
                return Long.parseLong(userIdHeader);
            } catch (NumberFormatException e) {
                logger.warn("Invalid X-User-Id header: {}", userIdHeader);
            }
        }
        return null;
    }

    // Helper method to get user email from Gateway headers
    private String getUserEmailFromHeaders(HttpServletRequest request) {
        return request.getHeader("X-User-Email");
    }

    // Helper method to extract email from JWT token - FALLBACK
    private String extractEmailFromToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            logger.warn("No authorization token provided or invalid token format");
            return null;
        }
        
        String token = authorization.substring(7); // Remove "Bearer " prefix
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim("email").asString();
        } catch (Exception e) {
            logger.error("Error decoding JWT token: {}", e.getMessage());
            return null;
        }
    }
    
    // Method to get user ID from user service based on email - FALLBACK
    private Long getUserIdFromUserService(String email, String authorization) {
        if (email == null) {
            logger.warn("Cannot get user ID - no email provided");
            return null;
        }
        
        try {
            logger.info("Getting user ID from user service for email: {}", email);
            UserDto userDto = userServiceClient.getUserByEmail(email, authorization);
            logger.info("Retrieved user: {}", userDto);
            return userDto.getId();
        } catch (Exception e) {
            logger.error("Error getting user ID from user service: {}", e.getMessage(), e);
            return null;
        }
    }

    // Get the current user's cart - PRIORITY: Use Gateway headers first
    @GetMapping("/current")
    public ResponseEntity<CartResponse> getCurrentUserCart(
            HttpServletRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        logger.info("=== Getting current user cart ===");
        
        // PRIORITY 1: Try to get user ID from Gateway headers
        Long userId = getUserIdFromHeaders(request);
        String userEmail = getUserEmailFromHeaders(request);
        
        logger.info("Gateway headers - UserId: {}, Email: {}", userId, userEmail);
        
        if (userId != null) {
            logger.info("Using userId from Gateway headers: {}", userId);
            CartResponse cartResponse = cartService.getCart(userId);
            return ResponseEntity.ok(cartResponse);
        }
        
        // FALLBACK: Try JWT token approach
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String tokenEmail = extractEmailFromToken(authorization);
            
            if (tokenEmail != null) {
                logger.info("Fallback: Getting cart for user email from token: {}", tokenEmail);
                Long userIdFromService = getUserIdFromUserService(tokenEmail, authorization);
                
                if (userIdFromService != null) {
                    CartResponse cartResponse = cartService.getCart(userIdFromService);
                    return ResponseEntity.ok(cartResponse);
                }
            }
        }
        
        // For guests or if user lookup fails, return an empty cart
        logger.info("No valid user identification - returning empty guest cart");
        CartResponse cartResponse = new CartResponse();
        cartResponse.setItems(new ArrayList<>());
        cartResponse.setTotalAmount(java.math.BigDecimal.ZERO);
        return ResponseEntity.ok(cartResponse);
    }

    // Add item to current user's cart - PRIORITY: Use Gateway headers first
    @PostMapping("/current/items")
    public ResponseEntity<CartResponse> addItemToCurrentUserCart(
            HttpServletRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody AddItemRequest addItemRequest) {
        
        logger.info("=== Adding item to cart ===");
        
        // PRIORITY 1: Try to get user ID from Gateway headers
        Long userId = getUserIdFromHeaders(request);
        String userEmail = getUserEmailFromHeaders(request);
        
        logger.info("Gateway headers - UserId: {}, Email: {}, ProductId: {}", 
                   userId, userEmail, addItemRequest.getProductId());
        
        if (userId != null) {
            logger.info("Using userId from Gateway headers: {}", userId);
            CartResponse cartResponse = cartService.addItemToCart(userId, addItemRequest);
            return new ResponseEntity<>(cartResponse, HttpStatus.CREATED);
        }
        
        // FALLBACK: Try JWT token approach
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String tokenEmail = extractEmailFromToken(authorization);
            logger.info("Fallback: Adding item for user email from token: {}", tokenEmail);
            
            try {
                Long userIdFromService = getUserIdFromUserService(tokenEmail, authorization);
                if (userIdFromService != null) {
                    CartResponse cartResponse = cartService.addItemToCart(userIdFromService, addItemRequest);
                    return new ResponseEntity<>(cartResponse, HttpStatus.CREATED);
                }
            } catch (Exception e) {
                logger.error("Error adding item to cart via fallback method: {}", e.getMessage(), e);
            }
        }
        
        // If no valid user identification, return authentication required
        logger.warn("No valid user identification for adding item - productId: {}", addItemRequest.getProductId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Authentication required");
        response.put("message", "Please log in to add items to your cart");
        
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(null);
    }

    // Debug endpoint to verify authentication and Gateway headers
    @GetMapping("/debug-auth")
    public ResponseEntity<Map<String, Object>> debugAuth(
            HttpServletRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        Map<String, Object> response = new HashMap<>();
        
        // Log all headers for debugging
        Map<String, String> headersMap = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            headersMap.put(name, request.getHeader(name));
        }
        response.put("headers", headersMap);
        
        // Check Gateway headers
        String userIdHeader = request.getHeader("X-User-Id");
        String userEmailHeader = request.getHeader("X-User-Email");
        String userRolesHeader = request.getHeader("X-User-Roles");
        
        response.put("gatewayHeaders", Map.of(
            "X-User-Id", userIdHeader != null ? userIdHeader : "NOT_SET",
            "X-User-Email", userEmailHeader != null ? userEmailHeader : "NOT_SET",
            "X-User-Roles", userRolesHeader != null ? userRolesHeader : "NOT_SET"
        ));
        
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            response.put("status", "No authorization header provided");
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);
        }
        
        String token = authorization.substring(7);
        try {
            DecodedJWT jwt = JWT.decode(token);
            
            // Check if token is expired
            if (jwt.getExpiresAt() != null && jwt.getExpiresAt().before(new Date())) {
                response.put("status", "Token expired");
                response.put("expiredAt", jwt.getExpiresAt());
                response.put("currentTime", new Date());
                return ResponseEntity.ok(response);
            }
            
            response.put("status", "Authenticated");
            response.put("email", jwt.getClaim("email").asString());
            response.put("userId", jwt.getClaim("userId").asString());
            response.put("roles", jwt.getClaim("roles"));
            response.put("issuedAt", jwt.getIssuedAt());
            response.put("expiresAt", jwt.getExpiresAt());
            response.put("claims", jwt.getClaims());
            
            // Test UserService connection through Gateway if we have email header
            if (userEmailHeader != null) {
                try {
                    UserDto userDto = userServiceClient.getUserByEmail(userEmailHeader, authorization);
                    response.put("userServiceTest", "SUCCESS");
                    response.put("retrievedUser", Map.of(
                        "id", userDto.getId(),
                        "email", userDto.getEmail(),
                        "firstName", userDto.getFirstName(),
                        "lastName", userDto.getLastName()
                    ));
                } catch (Exception e) {
                    response.put("userServiceTest", "FAILED");
                    response.put("userServiceError", e.getMessage());
                }
            } else {
                response.put("userServiceTest", "SKIPPED - No email header from Gateway");
            }
            
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "Invalid token");
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);
        }
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception e) {
        logger.error("Unhandled exception in cart controller: {}", e.getMessage(), e);
        
        Map<String, String> response = new HashMap<>();
        response.put("error", "An unexpected error occurred: " + e.getMessage());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
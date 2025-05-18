package com.example.controller;

import com.example.client.UserServiceClient;
import com.example.dto.*;
import com.example.service.CartService;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class CartController {
    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartService cartService;
    
    @Autowired
    private UserServiceClient userServiceClient;

    // Get the current user's cart
    @GetMapping("/current")
    public ResponseEntity<CartResponse> getCurrentUserCart(
            @RequestHeader("X-User-Email") String userEmail,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader("Authorization") String authorization) {
        
        logger.info("Getting cart for user: {}", userEmail);
        Long userIdLong = getUserIdFromHeader(userId, userEmail, authorization);
        
        CartResponse cartResponse = cartService.getCart(userIdLong);
        return ResponseEntity.ok(cartResponse);
    }

    // Get a specific user's cart (admin or original user only)
    @GetMapping("/{userId}")
    public ResponseEntity<CartResponse> getCart(
            @PathVariable Long userId,
            @RequestHeader("X-User-Email") String userEmail,
            @RequestHeader(value = "X-User-Roles", required = false) String roles) {
        
        logger.info("Getting cart for userId: {} by user: {}", userId, userEmail);
        CartResponse cartResponse = cartService.getCart(userId);
        return ResponseEntity.ok(cartResponse);
    }

    // Add item to current user's cart
    @PostMapping("/current/items")
    public ResponseEntity<CartResponse> addItemToCurrentUserCart(
            @RequestHeader("X-User-Email") String userEmail,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader("Authorization") String authorization,
            @RequestBody AddItemRequest request) {
        
        logger.info("Adding item to cart for user: {}, productId: {}", userEmail, request.getProductId());
        Long userIdLong = getUserIdFromHeader(userId, userEmail, authorization);
        
        CartResponse cartResponse = cartService.addItemToCart(userIdLong, request);
        return new ResponseEntity<>(cartResponse, HttpStatus.CREATED);
    }

    // Add item to a specific user's cart
    @PostMapping("/{userId}/items")
    public ResponseEntity<CartResponse> addItemToCart(
            @PathVariable Long userId,
            @RequestBody AddItemRequest request) {
        
        logger.info("Adding item to cart for userId: {}, productId: {}", userId, request.getProductId());
        CartResponse cartResponse = cartService.addItemToCart(userId, request);
        return new ResponseEntity<>(cartResponse, HttpStatus.CREATED);
    }

    // Update item in current user's cart
    @PutMapping("/current/items/{itemId}")
    public ResponseEntity<CartResponse> updateCurrentUserItemQuantity(
            @RequestHeader("X-User-Email") String userEmail,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long itemId,
            @RequestBody UpdateItemRequest request) {
        
        logger.info("Updating item quantity in cart for user: {}, itemId: {}", userEmail, itemId);
        Long userIdLong = getUserIdFromHeader(userId, userEmail, authorization);
        
        CartResponse cartResponse = cartService.updateItemQuantity(userIdLong, itemId, request);
        return ResponseEntity.ok(cartResponse);
    }

    // Update item in a specific user's cart
    @PutMapping("/{userId}/items/{itemId}")
    public ResponseEntity<CartResponse> updateItemQuantity(
            @PathVariable Long userId,
            @PathVariable Long itemId,
            @RequestBody UpdateItemRequest request) {
        
        logger.info("Updating item quantity in cart for userId: {}, itemId: {}", userId, itemId);
        CartResponse cartResponse = cartService.updateItemQuantity(userId, itemId, request);
        return ResponseEntity.ok(cartResponse);
    }

    // Remove item from current user's cart
    @DeleteMapping("/current/items/{itemId}")
    public ResponseEntity<CartResponse> removeItemFromCurrentUserCart(
            @RequestHeader("X-User-Email") String userEmail,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long itemId) {
        
        logger.info("Removing item from cart for user: {}, itemId: {}", userEmail, itemId);
        Long userIdLong = getUserIdFromHeader(userId, userEmail, authorization);
        
        CartResponse cartResponse = cartService.removeItemFromCart(userIdLong, itemId);
        return ResponseEntity.ok(cartResponse);
    }

    // Remove item from a specific user's cart
    @DeleteMapping("/{userId}/items/{itemId}")
    public ResponseEntity<CartResponse> removeItemFromCart(
            @PathVariable Long userId,
            @PathVariable Long itemId) {
        
        logger.info("Removing item from cart for userId: {}, itemId: {}", userId, itemId);
        CartResponse cartResponse = cartService.removeItemFromCart(userId, itemId);
        return ResponseEntity.ok(cartResponse);
    }

    // Clear current user's cart
    @DeleteMapping("/current")
    public ResponseEntity<Void> clearCurrentUserCart(
            @RequestHeader("X-User-Email") String userEmail,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader("Authorization") String authorization) {
        
        logger.info("Clearing cart for user: {}", userEmail);
        Long userIdLong = getUserIdFromHeader(userId, userEmail, authorization);
        
        cartService.clearCart(userIdLong);
        return ResponseEntity.noContent().build();
    }

    // Clear a specific user's cart
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> clearCart(@PathVariable Long userId) {
        logger.info("Clearing cart for userId: {}", userId);
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
// Add this debug endpoint to your CartController.java

@GetMapping("/debug")
public ResponseEntity<Map<String, Object>> debugRequest(
        @RequestHeader(value = "X-User-Email", required = false) String userEmail,
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @RequestHeader(value = "Origin", required = false) String origin,
        HttpServletRequest request) {
    
    Map<String, Object> debug = new HashMap<>();
    debug.put("timestamp", LocalDateTime.now());
    debug.put("method", request.getMethod());
    debug.put("path", request.getRequestURI());
    debug.put("userEmail", userEmail);
    debug.put("userId", userId);
    debug.put("authHeader", authorization != null ? "Present (Bearer " + authorization.substring(7, 17) + "...)" : "Missing");
    debug.put("origin", origin);
    
    // Log all headers for debugging
    Map<String, String> allHeaders = new HashMap<>();
    Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
        String headerName = headerNames.nextElement();
        allHeaders.put(headerName, request.getHeader(headerName));
    }
    debug.put("allHeaders", allHeaders);
    
    // Check if user can be retrieved
    if (userEmail != null && authorization != null) {
        try {
            Long userIdLong = getUserIdFromUserService(userEmail, authorization);
            debug.put("retrievedUserId", userIdLong);
            debug.put("userServiceConnection", "Success");
        } catch (Exception e) {
            debug.put("userServiceError", e.getMessage());
            debug.put("userServiceConnection", "Failed");
        }
    }
    
    logger.info("Debug request details: {}", debug);
    return ResponseEntity.ok(debug);
}
    // Checkout current user's cart
    @PostMapping("/current/checkout")
    public ResponseEntity<Void> checkoutCurrentUserCart(
            @RequestHeader("X-User-Email") String userEmail,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader("Authorization") String authorization,
            @RequestBody CheckoutRequest request) {
        
        logger.info("Checkout for user: {}", userEmail);
        Long userIdLong = getUserIdFromHeader(userId, userEmail, authorization);
        
        cartService.checkout(userIdLong, request);
        return ResponseEntity.noContent().build();
    }

    // Checkout a specific user's cart
    @PostMapping("/{userId}/checkout")
    public ResponseEntity<Void> checkout(
            @PathVariable Long userId,
            @RequestBody CheckoutRequest request) {
        
        logger.info("Checkout for userId: {}", userId);
        cartService.checkout(userId, request);
        return ResponseEntity.noContent().build();
    }
    
    // Helper method to extract userId from header or get it from user service
    private Long getUserIdFromHeader(String userId, String userEmail, String authorization) {
        if (userId != null && !userId.isEmpty()) {
            try {
                return Long.parseLong(userId);
            } catch (NumberFormatException e) {
                logger.warn("Invalid user ID format in header: {}", userId);
            }
        }
        
        // If userId header is not available, get the user ID from the user service via API Gateway
        return getUserIdFromUserService(userEmail, authorization);
    }
    
    // Method to get user ID from user service based on email
    private Long getUserIdFromUserService(String email, String authorization) {
        try {
            logger.info("Getting user ID from user service for email: {}", email);
            UserDto userDto = userServiceClient.getUserByEmail(email, authorization);
            logger.info("Retrieved user: {}", userDto);
            return userDto.getId();
        } catch (Exception e) {
            logger.error("Error getting user ID from user service: {}", e.getMessage(), e);
            throw new RuntimeException("Could not retrieve user ID for email: " + email, e);
        }
    }
}
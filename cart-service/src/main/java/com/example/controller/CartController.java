package com.example.controller;

import com.example.dto.AddItemRequest;
import com.example.dto.CartResponse;
import com.example.dto.CheckoutRequest;
import com.example.dto.UpdateItemRequest;
import com.example.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/{userId}")
    public ResponseEntity<CartResponse> getCart(@PathVariable Long userId) {
        CartResponse cartResponse = cartService.getCart(userId);
        return ResponseEntity.ok(cartResponse);
    }

    @PostMapping("/{userId}/items")
    public ResponseEntity<CartResponse> addItemToCart(
            @PathVariable Long userId,
            @RequestBody AddItemRequest request) {
        CartResponse cartResponse = cartService.addItemToCart(userId, request);
        return new ResponseEntity<>(cartResponse, HttpStatus.CREATED);
    }

    @PutMapping("/{userId}/items/{itemId}")
    public ResponseEntity<CartResponse> updateItemQuantity(
            @PathVariable Long userId,
            @PathVariable Long itemId,
            @RequestBody UpdateItemRequest request) {
        CartResponse cartResponse = cartService.updateItemQuantity(userId, itemId, request);
        return ResponseEntity.ok(cartResponse);
    }

    @DeleteMapping("/{userId}/items/{itemId}")
    public ResponseEntity<CartResponse> removeItemFromCart(
            @PathVariable Long userId,
            @PathVariable Long itemId) {
        CartResponse cartResponse = cartService.removeItemFromCart(userId, itemId);
        return ResponseEntity.ok(cartResponse);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> clearCart(@PathVariable Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/checkout")
    public ResponseEntity<Void> checkout(
            @PathVariable Long userId,
            @RequestBody CheckoutRequest request) {
        cartService.checkout(userId, request);
        return ResponseEntity.noContent().build();
    }
}

package com.example.service;

import com.example.dto.AddItemRequest;
import com.example.dto.CartResponse;
import com.example.dto.CheckoutRequest;
import com.example.dto.UpdateItemRequest;

public interface CartService {

    CartResponse getCart(Long userId);
    CartResponse addItemToCart(Long userId, AddItemRequest request);
    CartResponse updateItemQuantity(Long userId, Long itemId, UpdateItemRequest request);
    CartResponse removeItemFromCart(Long userId, Long itemId);
    void clearCart(Long userId);
    void checkout(Long userId, CheckoutRequest request);
}

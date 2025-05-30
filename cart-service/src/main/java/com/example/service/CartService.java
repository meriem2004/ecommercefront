package com.example.service;

import com.example.dto.*;
import com.example.model.Cart;
import com.example.model.CartItem;
import com.example.repository.CartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartService {
    
    private static final Logger logger = LoggerFactory.getLogger(CartService.class);
    
    @Autowired
    private CartRepository cartRepository;
    
    public CartResponse getCart(Long userId) {
        logger.info("Getting cart for user: {}", userId);
        
        Optional<Cart> cartOpt = cartRepository.findByUserId(userId);
        Cart cart = cartOpt.orElseGet(() -> createEmptyCart(userId));
        
        return convertToCartResponse(cart);
    }
    
    public CartResponse addItemToCart(Long userId, AddItemRequest request) {
        logger.info("Adding item to cart for user: {}, productId: {}", userId, request.getProductId());
        
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createEmptyCart(userId));
        
        // Check if item already exists in cart
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst();
        
        if (existingItem.isPresent()) {
            // Update quantity if item exists
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            logger.info("Updated existing item quantity to: {}", item.getQuantity());
        } else {
            // Add new item
            CartItem newItem = new CartItem();
            newItem.setProductId(request.getProductId());
            newItem.setProductName(request.getName());
            newItem.setPrice(request.getPrice());
            newItem.setQuantity(request.getQuantity());
            newItem.setCart(cart);
            cart.getItems().add(newItem);
            logger.info("Added new item to cart: {}", newItem.getProductName());
        }
        
        // Recalculate total
        cart.setTotalAmount(calculateTotal(cart.getItems()));
        
        Cart savedCart = cartRepository.save(cart);
        logger.info("Cart saved successfully with {} items", savedCart.getItems().size());
        
        return convertToCartResponse(savedCart);
    }
    
    public CartResponse syncCart(Long userId, List<CartSyncRequest.Item> items) {
        logger.info("Syncing cart for user: {} with {} items", userId, items.size());
        
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createEmptyCart(userId));
        
        // Clear existing items
        cart.getItems().clear();
        
        // Add all items from sync request
        for (CartSyncRequest.Item syncItem : items) {
            CartItem cartItem = new CartItem();
            cartItem.setProductId(Long.parseLong(syncItem.getProductId()));
            cartItem.setProductName(syncItem.getName());
            cartItem.setPrice(BigDecimal.valueOf(syncItem.getPrice()));
            cartItem.setQuantity(syncItem.getQuantity());
            cartItem.setCart(cart);
            cart.getItems().add(cartItem);
        }
        
        // Recalculate total
        cart.setTotalAmount(calculateTotal(cart.getItems()));
        
        Cart savedCart = cartRepository.save(cart);
        logger.info("Cart synced successfully with {} items", savedCart.getItems().size());
        
        return convertToCartResponse(savedCart);
    }
    
    private Cart createEmptyCart(Long userId) {
        logger.info("Creating empty cart for user: {}", userId);
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setTotalAmount(BigDecimal.ZERO);
        cart.setItems(new ArrayList<>());
        return cartRepository.save(cart);
    }
    
    private BigDecimal calculateTotal(List<CartItem> items) {
        return items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private CartResponse convertToCartResponse(Cart cart) {
        CartResponse response = new CartResponse();
        response.setUserId(cart.getUserId());
        response.setTotalAmount(cart.getTotalAmount());
        
        List<CartItemDto> itemDtos = cart.getItems().stream()
                .map(item -> {
                    CartItemDto dto = new CartItemDto();
                    dto.setId(item.getId());
                    dto.setProductId(item.getProductId());
                    dto.setProductName(item.getProductName());
                    dto.setQuantity(item.getQuantity());
                    dto.setPrice(item.getPrice());
                    dto.setSubtotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                    return dto;
                })
                .collect(Collectors.toList());
        
        response.setItems(itemDtos);
        response.setTotalItems(itemDtos.stream().mapToInt(CartItemDto::getQuantity).sum());
        return response;
    }
}
package com.example.service;

import com.example.dto.AddItemRequest;
import com.example.dto.CartItemDto;
import com.example.dto.CartResponse;
import com.example.dto.CartSyncItemDto;
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
        
        Cart cart = cartRepository.findByUserId(userId)
                .orElse(createEmptyCart(userId));
        
        return convertToCartResponse(cart);
    }

    public CartResponse addItemToCart(Long userId, AddItemRequest addItemRequest) {
        logger.info("Adding item to cart for user: {}, productId: {}", userId, addItemRequest.getProductId());
        
        Cart cart = cartRepository.findByUserId(userId)
                .orElse(createEmptyCart(userId));
        
        // Check if item already exists in cart
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(addItemRequest.getProductId()))
                .findFirst();
        
        if (existingItem.isPresent()) {
            // Update quantity of existing item
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + addItemRequest.getQuantity());
            logger.info("Updated existing item quantity: {}", item.getQuantity());
        } else {
            // Add new item to cart
            CartItem newItem = new CartItem();
            newItem.setProductId(addItemRequest.getProductId());
            newItem.setProductName(addItemRequest.getName());
            newItem.setQuantity(addItemRequest.getQuantity());
            newItem.setPrice(addItemRequest.getPrice());
            newItem.setCart(cart);
            
            cart.getItems().add(newItem);
            logger.info("Added new item to cart: {}", addItemRequest.getName());
        }
        
        // Update total amount
        updateCartTotal(cart);
        
        // Save cart
        Cart savedCart = cartRepository.save(cart);
        logger.info("Cart saved successfully with {} items", savedCart.getItems().size());
        
        return convertToCartResponse(savedCart);
    }

    // ✅ NEW: Update item quantity
    public CartResponse updateItemQuantity(Long userId, Long productId, Integer quantity) {
        logger.info("Updating item quantity for user: {}, productId: {}, quantity: {}", userId, productId, quantity);
        
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));
        
        // Find the item to update
        Optional<CartItem> itemToUpdate = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();
        
        if (itemToUpdate.isPresent()) {
            CartItem item = itemToUpdate.get();
            if (quantity <= 0) {
                // Remove item if quantity is 0 or negative
                cart.getItems().remove(item);
                logger.info("Removed item from cart: productId {}", productId);
            } else {
                // Update quantity
                item.setQuantity(quantity);
                logger.info("Updated item quantity to: {}", quantity);
            }
            
            // Update total amount
            updateCartTotal(cart);
            
            // Save cart
            Cart savedCart = cartRepository.save(cart);
            logger.info("Cart updated successfully with {} items", savedCart.getItems().size());
            
            return convertToCartResponse(savedCart);
        } else {
            throw new RuntimeException("Item not found in cart: productId " + productId);
        }
    }

    // ✅ NEW: Remove item from cart
    public CartResponse removeItemFromCart(Long userId, Long productId) {
        logger.info("Removing item from cart for user: {}, productId: {}", userId, productId);
        
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));
        
        // Find and remove the item
        boolean removed = cart.getItems().removeIf(item -> item.getProductId().equals(productId));
        
        if (removed) {
            logger.info("Item removed from cart: productId {}", productId);
            
            // Update total amount
            updateCartTotal(cart);
            
            // Save cart
            Cart savedCart = cartRepository.save(cart);
            logger.info("Cart updated successfully with {} items", savedCart.getItems().size());
            
            return convertToCartResponse(savedCart);
        } else {
            throw new RuntimeException("Item not found in cart: productId " + productId);
        }
    }

    // ✅ NEW: Clear entire cart
    public CartResponse clearCart(Long userId) {
        logger.info("Clearing cart for user: {}", userId);
        
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));
        
        // Clear all items
        cart.getItems().clear();
        cart.setTotalAmount(BigDecimal.ZERO);
        
        // Save cart
        Cart savedCart = cartRepository.save(cart);
        logger.info("Cart cleared successfully for user: {}", userId);
        
        return convertToCartResponse(savedCart);
    }

    public CartResponse syncCart(Long userId, List<CartSyncItemDto> items) {
        logger.info("Syncing cart for user: {} with {} items", userId, items != null ? items.size() : 0);
        
        Cart cart = cartRepository.findByUserId(userId)
                .orElse(createEmptyCart(userId));
        
        // Clear existing items
        cart.getItems().clear();
        
        // Add synced items
        if (items != null) {
            for (CartSyncItemDto syncItem : items) {
                CartItem cartItem = new CartItem();
                cartItem.setProductId(syncItem.getProductId());
                // The following two lines are commented out because CartSyncItemDto does not have getName() or getPrice()
                // cartItem.setProductName(syncItem.getName());
                // cartItem.setPrice(BigDecimal.valueOf(syncItem.getPrice()));
                cartItem.setQuantity(syncItem.getQuantity());
                cartItem.setCart(cart);
                
                cart.getItems().add(cartItem);
            }
        }
        
        // Update total amount
        updateCartTotal(cart);
        
        // Save cart
        Cart savedCart = cartRepository.save(cart);
        logger.info("Cart synced successfully with {} items", savedCart.getItems().size());
        
        return convertToCartResponse(savedCart);
    }

    private Cart createEmptyCart(Long userId) {
        logger.info("Creating empty cart for user: {}", userId);
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setItems(new ArrayList<>());
        cart.setTotalAmount(BigDecimal.ZERO);
        return cart;
    }

    private void updateCartTotal(Cart cart) {
        BigDecimal total = cart.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        cart.setTotalAmount(total);
        logger.debug("Updated cart total to: {}", total);
    }

    private CartResponse convertToCartResponse(Cart cart) {
        CartResponse response = new CartResponse();
        response.setUserId(cart.getUserId());
        response.setTotalAmount(cart.getTotalAmount());
        response.setTotalItems(cart.getItems().size());
        
        List<CartItemDto> itemDtos = cart.getItems().stream()
                .map(this::convertToCartItemDto)
                .collect(Collectors.toList());
        
        response.setItems(itemDtos);
        
        return response;
    }

    private CartItemDto convertToCartItemDto(CartItem cartItem) {
        CartItemDto dto = new CartItemDto();
        dto.setId(cartItem.getId());
        dto.setProductId(cartItem.getProductId());
        dto.setProductName(cartItem.getProductName());
        dto.setQuantity(cartItem.getQuantity());
        dto.setPrice(cartItem.getPrice());
        
        return dto;
    }
}
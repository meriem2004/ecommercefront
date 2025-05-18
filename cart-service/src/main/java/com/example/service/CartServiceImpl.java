package com.example.service;

// Ensure the correct package path for ProductServiceClient
import com.example.client.ProductServiceClient; // Verify this path or update it to the correct one
import com.example.dto.*;
import com.example.model.Cart;
import com.example.model.CartItem;
import com.example.repository.CartRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductServiceClient productServiceClient;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public CartResponse getCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return mapToCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addItemToCart(Long userId, AddItemRequest request) {
        Cart cart = getOrCreateCart(userId);

        ProductDto product = productServiceClient.getProductById(request.getProductId());

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            // Update existing item quantity
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
        } else {
            // Add new item to cart
            CartItem newItem = new CartItem();
            newItem.setProductId(product.getId());
            newItem.setProductName(product.getName());
            newItem.setQuantity(request.getQuantity());
            newItem.setPrice(new BigDecimal(String.valueOf(product.getPrice())));
            newItem.setCart(cart);
            cart.getItems().add(newItem);
        }

        updateCartTotals(cart);
        cartRepository.save(cart);

        return mapToCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse updateItemQuantity(Long userId, Long itemId, UpdateItemRequest request) {
        Cart cart = getOrCreateCart(userId);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cart item not found with id: " + itemId));

        item.setQuantity(request.getQuantity());

        if (item.getQuantity() <= 0) {
            cart.getItems().remove(item);
        }

        updateCartTotals(cart);
        cartRepository.save(cart);

        return mapToCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse removeItemFromCart(Long userId, Long itemId) {
        Cart cart = getOrCreateCart(userId);

        cart.getItems().removeIf(item -> item.getId().equals(itemId));

        updateCartTotals(cart);
        cartRepository.save(cart);

        return mapToCartResponse(cart);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);

        cart.getItems().clear();
        cart.setTotalAmount(BigDecimal.ZERO);

        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void checkout(Long userId, CheckoutRequest request) {
        Cart cart = getOrCreateCart(userId);

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cannot checkout an empty cart");
        }

        // Create order request
        Map<String, Object> orderRequest = new HashMap<>();
        orderRequest.put("userId", userId);
        orderRequest.put("shippingAddress", request.getShippingAddress());

        List<Map<String, Object>> orderItems = cart.getItems().stream()
                .map(item -> {
                    Map<String, Object> orderItem = new HashMap<>();
                    orderItem.put("productId", item.getProductId());
                    orderItem.put("quantity", item.getQuantity());
                    return orderItem;
                })
                .collect(Collectors.toList());

        orderRequest.put("orderItems", orderItems);

        // Call order service to create order
        restTemplate.postForEntity("http://order-service/api/orders", orderRequest, Object.class);

        // Clear the cart after successful checkout
        clearCart(userId);
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findById(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    newCart.setTotalAmount(BigDecimal.ZERO);
                    return cartRepository.save(newCart);
                });
    }

    private void updateCartTotals(Cart cart) {
        BigDecimal total = cart.getItems().stream()
                .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setTotalAmount(total);
    }

    private CartResponse mapToCartResponse(Cart cart) {
        List<CartItemDto> itemDtos = cart.getItems().stream()
                .map(item -> {
                    BigDecimal subtotal = item.getPrice().multiply(new BigDecimal(item.getQuantity()));
                    return new CartItemDto(
                            item.getId(),
                            item.getProductId(),
                            item.getProductName(),
                            item.getQuantity(),
                            item.getPrice(),
                            subtotal
                    );
                })
                .collect(Collectors.toList());

        int totalItems = cart.getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        return new CartResponse(
                cart.getUserId(),
                itemDtos,
                cart.getTotalAmount(),
                totalItems
        );
    }
}

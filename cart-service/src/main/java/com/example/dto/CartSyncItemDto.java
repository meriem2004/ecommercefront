package com.example.dto;

public class CartSyncItemDto {
    private Long productId;
    private Integer quantity;

    // Constructors
    public CartSyncItemDto() {}

    public CartSyncItemDto(Long productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    // Getters and setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
} 
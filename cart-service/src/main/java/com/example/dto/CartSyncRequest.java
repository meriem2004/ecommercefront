package com.example.dto;

import java.util.List;

public class CartSyncRequest {
    private Long userId;
    private List<Item> items;

    // Getters and setters

    public static class Item {
        private String productId;
        private String name;
        private double price;
        private int quantity;

        // Getters and setters
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }
} 
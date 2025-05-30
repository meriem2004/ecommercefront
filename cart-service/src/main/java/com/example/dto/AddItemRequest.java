package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddItemRequest {

    private Long productId;
    private String name;        // Added - needed for cart sync
    private BigDecimal price;   // Added - needed for cart sync
    private int quantity;
}
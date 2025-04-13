package com.example.service;

import com.example.dto.OrderRequest;
import com.example.dto.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(OrderRequest orderRequest);
    OrderResponse getOrderById(Long id);
    List<OrderResponse> getOrdersByUserId(Long userId);
    OrderResponse updateOrderStatus(Long id, String status);
}

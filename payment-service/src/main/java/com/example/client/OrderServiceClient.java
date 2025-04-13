package com.example.client;

import com.example.dto.OrderDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "order-service")
public interface OrderServiceClient {

    @GetMapping("/api/orders/{id}")
    OrderDto getOrderById(@PathVariable("id") Long id);

    @GetMapping("/api/orders/number/{orderNumber}")
    OrderDto getOrderByNumber(@PathVariable("orderNumber") String orderNumber);

    @PutMapping("/api/orders/{id}/status")
    OrderDto updateOrderStatus(@PathVariable("id") Long id, @RequestParam("status") String status);
}

package com.example.service.gateway;

import com.example.dto.PaymentRequest;

import java.math.BigDecimal;

public interface PaymentGateway {
    String processPayment(PaymentRequest paymentRequest);
    void refundPayment(String transactionId, BigDecimal amount);
}

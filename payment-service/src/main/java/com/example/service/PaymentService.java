package com.example.service;

import com.example.dto.PaymentRequest;
import com.example.dto.PaymentResponse;
import com.example.dto.RefundRequest;

import java.util.List;

public interface PaymentService {

    PaymentResponse processPayment(PaymentRequest paymentRequest);
    PaymentResponse getPaymentById(Long id);
    List<PaymentResponse> getPaymentsByUserId(Long userId);
    List<PaymentResponse> getPaymentsByOrderId(Long orderId);
    PaymentResponse getPaymentByOrderNumber(String orderNumber);
    PaymentResponse refundPayment(RefundRequest refundRequest);
    PaymentResponse cancelPayment(String paymentNumber);
}

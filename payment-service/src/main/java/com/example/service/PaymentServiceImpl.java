package com.example.service;

import com.example.client.OrderServiceClient;
import com.example.dto.OrderDto;
import com.example.dto.PaymentRequest;
import com.example.dto.PaymentResponse;
import com.example.dto.RefundRequest;
import com.example.model.Payment;
import com.example.repository.PaymentRepository;
import com.example.service.gateway.PaymentGateway;
import com.example.service.gateway.PaymentGatewayFactory;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderServiceClient orderServiceClient;

    @Autowired
    private PaymentGatewayFactory paymentGatewayFactory;

    @Override
    @Transactional
    public PaymentResponse processPayment(PaymentRequest paymentRequest) {
        // Validate order exists and amount matches
        OrderDto order = null;
        if (paymentRequest.getOrderId() != null) {
            order = orderServiceClient.getOrderById(paymentRequest.getOrderId());
        } else if (paymentRequest.getOrderNumber() != null) {
            order = orderServiceClient.getOrderByNumber(paymentRequest.getOrderNumber());
        } else {
            throw new RuntimeException("Either orderId or orderNumber must be provided");
        }

        if (order == null) {
            throw new RuntimeException("Order not found");
        }

        // Verify payment amount matches order amount
        if (paymentRequest.getAmount().compareTo(order.getTotalAmount()) != 0) {
            throw new RuntimeException("Payment amount does not match order amount");
        }

        // Create payment record
        Payment payment = new Payment();
        payment.setPaymentNumber(generatePaymentNumber());
        payment.setOrderId(order.getId());
        payment.setOrderNumber(order.getOrderNumber());
        payment.setUserId(order.getUserId());
        payment.setAmount(paymentRequest.getAmount());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatus("PENDING");
        payment.setPaymentMethod(paymentRequest.getPaymentMethod());

        // Save initial payment record
        payment = paymentRepository.save(payment);

        try {
            // Process payment through the appropriate payment gateway
            PaymentGateway paymentGateway = paymentGatewayFactory.getPaymentGateway(paymentRequest.getPaymentMethod());
            String transactionId = paymentGateway.processPayment(paymentRequest);

            // Update payment with transaction ID
            payment.setTransactionId(transactionId);
            payment.setStatus("COMPLETED");

            // Update order status to PAID
            orderServiceClient.updateOrderStatus(order.getId(), "PAID");
        } catch (Exception e) {
            // Update payment status on failure
            payment.setStatus("FAILED");
            payment.setErrorMessage(e.getMessage());
        }

        // Save updated payment record
        payment = paymentRepository.save(payment);

        return mapToPaymentResponse(payment);
    }

    @Override
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
        return mapToPaymentResponse(payment);
    }

    @Override
    public List<PaymentResponse> getPaymentsByUserId(Long userId) {
        List<Payment> payments = paymentRepository.findByUserId(userId);
        return payments.stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponse> getPaymentsByOrderId(Long orderId) {
        List<Payment> payments = paymentRepository.findByOrderId(orderId);
        return payments.stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentResponse getPaymentByOrderNumber(String orderNumber) {
        Payment payment = paymentRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Payment not found for order number: " + orderNumber));
        return mapToPaymentResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse refundPayment(RefundRequest refundRequest) {
        Payment payment = paymentRepository.findByPaymentNumber(refundRequest.getPaymentNumber())
                .orElseThrow(() -> new RuntimeException("Payment not found with number: " + refundRequest.getPaymentNumber()));

        if (!"COMPLETED".equals(payment.getStatus())) {
            throw new RuntimeException("Only completed payments can be refunded");
        }

        try {
            // Process refund through the payment gateway
            PaymentGateway paymentGateway = paymentGatewayFactory.getPaymentGateway(payment.getPaymentMethod());
            paymentGateway.refundPayment(payment.getTransactionId(), refundRequest.getAmount());

            // Update payment status
            payment.setStatus("REFUNDED");

            // Update order status
            orderServiceClient.updateOrderStatus(payment.getOrderId(), "REFUNDED");
        } catch (Exception e) {
            payment.setErrorMessage("Refund failed: " + e.getMessage());
        }

        payment = paymentRepository.save(payment);

        return mapToPaymentResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse cancelPayment(String paymentNumber) {
        Payment payment = paymentRepository.findByPaymentNumber(paymentNumber)
                .orElseThrow(() -> new RuntimeException("Payment not found with number: " + paymentNumber));

        if ("PENDING".equals(payment.getStatus())) {
            payment.setStatus("CANCELLED");
            payment = paymentRepository.save(payment);

            // Update order status
            orderServiceClient.updateOrderStatus(payment.getOrderId(), "PAYMENT_CANCELLED");
        } else {
            throw new RuntimeException("Only pending payments can be cancelled");
        }

        return mapToPaymentResponse(payment);
    }

    private String generatePaymentNumber() {
        return "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getPaymentNumber(),
                payment.getOrderId(),
                payment.getOrderNumber(),
                payment.getUserId(),
                payment.getAmount(),
                payment.getPaymentDate(),
                payment.getStatus(),
                payment.getPaymentMethod(),
                payment.getTransactionId(),
                payment.getErrorMessage()
        );
    }
}

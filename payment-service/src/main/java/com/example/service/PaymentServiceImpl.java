package com.example.service;

import com.example.dto.PaymentRequest;
import com.example.dto.PaymentResponse;
import com.example.dto.RefundRequest;
import com.example.model.Payment;
import com.example.repository.PaymentRepository;
import com.example.service.gateway.PaymentGateway;
import com.example.service.gateway.PaymentGatewayFactory;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentGatewayFactory paymentGatewayFactory;

    // Remove OrderServiceClient dependency for now
    // @Autowired
    // private OrderServiceClient orderServiceClient;

    @Override
    @Transactional
    public PaymentResponse processPayment(PaymentRequest paymentRequest) {
        logger.info("Processing payment request: {}", paymentRequest);
        
        try {
            // Basic validation
            if (paymentRequest.getAmount() == null || paymentRequest.getAmount().doubleValue() <= 0) {
                throw new RuntimeException("Invalid payment amount");
            }
            
            if (paymentRequest.getUserId() == null) {
                throw new RuntimeException("User ID is required");
            }
            
            if (paymentRequest.getPaymentMethod() == null || paymentRequest.getPaymentMethod().isEmpty()) {
                throw new RuntimeException("Payment method is required");
            }

            // Check for duplicate payments for the same order
            if (paymentRequest.getOrderId() != null) {
                List<Payment> existingPayments = paymentRepository.findByOrderId(paymentRequest.getOrderId());
                boolean hasCompletedPayment = existingPayments.stream()
                    .anyMatch(p -> "COMPLETED".equals(p.getStatus()));
                
                if (hasCompletedPayment) {
                    throw new RuntimeException("Order already has a completed payment");
                }
            }

            logger.info("Payment validation passed, creating payment record");

            // Create payment record with retry logic for unique constraint
            Payment payment = null;
            int retryCount = 0;
            int maxRetries = 3;
            
            while (payment == null && retryCount < maxRetries) {
                try {
                    payment = new Payment();
                    payment.setPaymentNumber(generatePaymentNumber());
                    payment.setOrderId(paymentRequest.getOrderId());
                    payment.setOrderNumber(paymentRequest.getOrderNumber());
                    payment.setUserId(paymentRequest.getUserId());
                    payment.setAmount(paymentRequest.getAmount());
                    payment.setPaymentDate(LocalDateTime.now());
                    payment.setStatus("PENDING");
                    payment.setPaymentMethod(paymentRequest.getPaymentMethod());

                    // Save initial payment record
                    payment = paymentRepository.save(payment);
                    logger.info("Payment record created with ID: {} and number: {}", payment.getId(), payment.getPaymentNumber());
                    
                } catch (Exception e) {
                    retryCount++;
                    logger.warn("Payment creation attempt {} failed: {}", retryCount, e.getMessage());
                    
                    if (retryCount >= maxRetries) {
                        throw new RuntimeException("Failed to create unique payment record after " + maxRetries + " attempts");
                    }
                    
                    payment = null; // Reset for retry
                    Thread.sleep(100); // Small delay before retry
                }
            }

            try {
                // Process payment through the appropriate payment gateway
                PaymentGateway paymentGateway = paymentGatewayFactory.getPaymentGateway(paymentRequest.getPaymentMethod());
                logger.info("Processing payment through gateway for method: {}", paymentRequest.getPaymentMethod());
                
                String transactionId = paymentGateway.processPayment(paymentRequest);

                // Update payment with transaction ID
                payment.setTransactionId(transactionId);
                payment.setStatus("COMPLETED");
                
                logger.info("Payment completed successfully with transaction ID: {}", transactionId);

                // TODO: Update order status to PAID when OrderServiceClient is available
                // orderServiceClient.updateOrderStatus(order.getId(), "PAID");
                
            } catch (Exception e) {
                logger.error("Payment processing failed: {}", e.getMessage(), e);
                // Update payment status on failure
                payment.setStatus("FAILED");
                payment.setErrorMessage(e.getMessage());
            }

            // Save updated payment record
            payment = paymentRepository.save(payment);
            logger.info("Payment record updated with status: {}", payment.getStatus());

            return mapToPaymentResponse(payment);
            
        } catch (Exception e) {
            logger.error("Payment processing error: {}", e.getMessage(), e);
            throw new RuntimeException("Payment processing failed: " + e.getMessage(), e);
        }
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

            // TODO: Update order status when OrderServiceClient is available
            // orderServiceClient.updateOrderStatus(payment.getOrderId(), "REFUNDED");
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

            // TODO: Update order status when OrderServiceClient is available
            // orderServiceClient.updateOrderStatus(payment.getOrderId(), "PAYMENT_CANCELLED");
        } else {
            throw new RuntimeException("Only pending payments can be cancelled");
        }

        return mapToPaymentResponse(payment);
    }

    private String generatePaymentNumber() {
        // Generate a more unique payment number with timestamp
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(6); // Last 7 digits
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "PAY-" + timestamp + "-" + uuid;
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
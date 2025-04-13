package com.example.service.gateway;

import com.example.dto.PaymentRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class PaypalPaymentGateway implements PaymentGateway {

    @Override
    public String processPayment(PaymentRequest paymentRequest) {
        // In a real implementation, this would make API calls to PayPal
        // For demo purposes, we'll simulate a successful payment

        // Validate PayPal details
        if (paymentRequest.getPaypalDetails() == null) {
            throw new RuntimeException("PayPal details are required");
        }

        validatePaypalDetails(paymentRequest);

        // Generate a mock transaction ID
        return "PP-" + UUID.randomUUID().toString();
    }

    @Override
    public void refundPayment(String transactionId, BigDecimal amount) {
        // In a real implementation, this would make API calls to PayPal
        // For demo purposes, we'll simulate a successful refund

        if (transactionId == null || !transactionId.startsWith("PP-")) {
            throw new RuntimeException("Invalid transaction ID for PayPal refund");
        }

        // Refund logic would go here
    }

    private void validatePaypalDetails(PaymentRequest paymentRequest) {
        String email = paymentRequest.getPaypalDetails().getEmail();
        String token = paymentRequest.getPaypalDetails().getToken();

        if (email == null || !email.contains("@")) {
            throw new RuntimeException("Invalid PayPal email");
        }

        if (token == null || token.isEmpty()) {
            throw new RuntimeException("PayPal token is required");
        }
    }
}

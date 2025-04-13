package com.example.service.gateway;

import com.example.dto.PaymentRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class CreditCardPaymentGateway implements PaymentGateway {

    @Override
    public String processPayment(PaymentRequest paymentRequest) {
        // In a real implementation, this would make API calls to a payment processor
        // For demo purposes, we'll simulate a successful payment

        // Validate credit card details
        if (paymentRequest.getCreditCardDetails() == null) {
            throw new RuntimeException("Credit card details are required");
        }

        validateCreditCardDetails(paymentRequest);

        // Generate a mock transaction ID
        return "CC-" + UUID.randomUUID().toString();
    }

    @Override
    public void refundPayment(String transactionId, BigDecimal amount) {
        // In a real implementation, this would make API calls to the payment processor
        // For demo purposes, we'll simulate a successful refund

        if (transactionId == null || !transactionId.startsWith("CC-")) {
            throw new RuntimeException("Invalid transaction ID for credit card refund");
        }

        // Refund logic would go here
    }

    private void validateCreditCardDetails(PaymentRequest paymentRequest) {
        // Basic validation - in a real system, you would use a dedicated library
        String cardNumber = paymentRequest.getCreditCardDetails().getCardNumber();
        String cvv = paymentRequest.getCreditCardDetails().getCvv();
        String expiryMonth = paymentRequest.getCreditCardDetails().getExpiryMonth();
        String expiryYear = paymentRequest.getCreditCardDetails().getExpiryYear();

        if (cardNumber == null || cardNumber.length() < 13 || cardNumber.length() > 19) {
            throw new RuntimeException("Invalid card number");
        }

        if (cvv == null || cvv.length() < 3 || cvv.length() > 4) {
            throw new RuntimeException("Invalid CVV");
        }

        // More validation would be added in a real implementation
    }
}

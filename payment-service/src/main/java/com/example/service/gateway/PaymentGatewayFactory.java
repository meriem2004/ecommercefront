package com.example.service.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PaymentGatewayFactory {

    @Autowired
    private CreditCardPaymentGateway creditCardPaymentGateway;

    @Autowired
    private PaypalPaymentGateway paypalPaymentGateway;

    public PaymentGateway getPaymentGateway(String paymentMethod) {
        switch (paymentMethod) {
            case "CREDIT_CARD":
                return creditCardPaymentGateway;
            case "PAYPAL":
                return paypalPaymentGateway;
            default:
                throw new RuntimeException("Unsupported payment method: " + paymentMethod);
        }
    }
}
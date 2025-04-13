package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private Long orderId;
    private String orderNumber;
    private Long userId;
    private BigDecimal amount;
    private String paymentMethod;
    private CreditCardDetails creditCardDetails;
    private PaypalDetails paypalDetails;
}

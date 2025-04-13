package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequest {
    private String paymentNumber;
    private String reason;
    private BigDecimal amount; // Optional for partial refunds
}

package com.example.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String paymentNumber;
    private Long orderId;
    private String orderNumber;
    private Long userId;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private String status; // PENDING, COMPLETED, FAILED, REFUNDED
    private String paymentMethod; // CREDIT_CARD, PAYPAL, etc.
    private String transactionId;
    private String errorMessage;

}

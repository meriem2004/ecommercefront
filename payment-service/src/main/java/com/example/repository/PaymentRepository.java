package com.example.repository;

import com.example.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUserId(Long userId);
    List<Payment> findByOrderId(Long orderId);
    Optional<Payment> findByOrderNumber(String orderNumber);
    Optional<Payment> findByPaymentNumber(String paymentNumber);
    Optional<Payment> findByTransactionId(String transactionId);
}

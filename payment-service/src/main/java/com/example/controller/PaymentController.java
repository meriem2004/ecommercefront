package com.example.controller;

import com.example.dto.PaymentRequest;
import com.example.dto.PaymentResponse;
import com.example.dto.RefundRequest;
import com.example.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(@RequestBody PaymentRequest paymentRequest) {
        PaymentResponse response = paymentService.processPayment(paymentRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id) {
        PaymentResponse response = paymentService.getPaymentById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByUserId(@PathVariable Long userId) {
        List<PaymentResponse> payments = paymentService.getPaymentsByUserId(userId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByOrderId(@PathVariable Long orderId) {
        List<PaymentResponse> payments = paymentService.getPaymentsByOrderId(orderId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/order/number/{orderNumber}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderNumber(@PathVariable String orderNumber) {
        PaymentResponse payment = paymentService.getPaymentByOrderNumber(orderNumber);
        return ResponseEntity.ok(payment);
    }

    @PostMapping("/refund")
    public ResponseEntity<PaymentResponse> refundPayment(@RequestBody RefundRequest refundRequest) {
        PaymentResponse response = paymentService.refundPayment(refundRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{paymentNumber}/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(@PathVariable String paymentNumber) {
        PaymentResponse response = paymentService.cancelPayment(paymentNumber);
        return ResponseEntity.ok(response);
    }
}

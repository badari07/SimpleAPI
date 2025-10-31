package com.example.payment.controller;

import com.example.payment.entity.Payment;
import com.example.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {
    
    @Autowired
    private PaymentService paymentService;
    
    @PostMapping
    public ResponseEntity<Payment> createPayment(
            @RequestParam Long orderId,
            @RequestParam Long userId,
            @RequestParam BigDecimal amount,
            @RequestParam Payment.PaymentMethod paymentMethod) {
        try {
            Payment payment = paymentService.createPayment(orderId, userId, amount, paymentMethod);
            return ResponseEntity.status(HttpStatus.CREATED).body(payment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/{id}/process")
    public ResponseEntity<Payment> processPayment(@PathVariable Long id) {
        try {
            Payment payment = paymentService.processPayment(id);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
        try {
            Payment payment = paymentService.getPaymentById(id);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<Payment> getPaymentByTransactionId(@PathVariable String transactionId) {
        try {
            Payment payment = paymentService.getPaymentByTransactionId(transactionId);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Payment>> getPaymentsByUserId(@PathVariable Long userId) {
        List<Payment> payments = paymentService.getPaymentsByUserId(userId);
        return ResponseEntity.ok(payments);
    }
    
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Payment>> getPaymentsByOrderId(@PathVariable Long orderId) {
        List<Payment> payments = paymentService.getPaymentsByOrderId(orderId);
        return ResponseEntity.ok(payments);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Payment>> getPaymentsByStatus(@PathVariable Payment.PaymentStatus status) {
        List<Payment> payments = paymentService.getPaymentsByStatus(status);
        return ResponseEntity.ok(payments);
    }
    
    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<List<Payment>> getUserPaymentsByStatus(
            @PathVariable Long userId, 
            @PathVariable Payment.PaymentStatus status) {
        List<Payment> payments = paymentService.getUserPaymentsByStatus(userId, status);
        return ResponseEntity.ok(payments);
    }
    
    @GetMapping("/order/{orderId}/status/{status}")
    public ResponseEntity<List<Payment>> getOrderPaymentsByStatus(
            @PathVariable Long orderId, 
            @PathVariable Payment.PaymentStatus status) {
        List<Payment> payments = paymentService.getOrderPaymentsByStatus(orderId, status);
        return ResponseEntity.ok(payments);
    }
    
    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments() {
        List<Payment> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<Payment> updatePaymentStatus(
            @PathVariable Long id, 
            @RequestParam Payment.PaymentStatus status) {
        try {
            Payment payment = paymentService.updatePaymentStatus(id, status);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/{id}/refund")
    public ResponseEntity<Payment> refundPayment(
            @PathVariable Long id, 
            @RequestParam String reason) {
        try {
            Payment payment = paymentService.refundPayment(id, reason);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/order/{orderId}/completed")
    public ResponseEntity<Boolean> isPaymentCompleted(@PathVariable Long orderId) {
        boolean isCompleted = paymentService.isPaymentCompleted(orderId);
        return ResponseEntity.ok(isCompleted);
    }
    
    @GetMapping("/order/{orderId}/total")
    public ResponseEntity<BigDecimal> getTotalAmountPaid(@PathVariable Long orderId) {
        BigDecimal totalAmount = paymentService.getTotalAmountPaid(orderId);
        return ResponseEntity.ok(totalAmount);
    }
}

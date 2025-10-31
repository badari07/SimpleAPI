package com.example.payment.service;

import com.example.payment.entity.Payment;
import com.example.payment.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class PaymentService {
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    public Payment createPayment(Long orderId, Long userId, BigDecimal amount, Payment.PaymentMethod paymentMethod) {
        Payment payment = new Payment(orderId, userId, amount, paymentMethod);
        payment.setStatus(Payment.PaymentStatus.PENDING);
        
        Payment savedPayment = paymentRepository.save(payment);
        
        // Send payment created event to Kafka
        kafkaTemplate.send("payment-events", "payment-created", savedPayment);
        
        return savedPayment;
    }
    
    public Payment processPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));
        
        if (payment.getStatus() != Payment.PaymentStatus.PENDING) {
            throw new RuntimeException("Payment is not in pending status");
        }
        
        payment.setStatus(Payment.PaymentStatus.PROCESSING);
        payment.setTransactionId(UUID.randomUUID().toString());
        
        Payment updatedPayment = paymentRepository.save(payment);
        
        // Simulate payment processing
        try {
            // In a real implementation, this would call a payment gateway
            Thread.sleep(2000); // Simulate processing time
            
            // Simulate success (90% success rate)
            if (Math.random() > 0.1) {
                payment.setStatus(Payment.PaymentStatus.COMPLETED);
                payment.setPaymentGatewayResponse("Payment processed successfully");
            } else {
                payment.setStatus(Payment.PaymentStatus.FAILED);
                payment.setFailureReason("Payment gateway declined the transaction");
            }
        } catch (InterruptedException e) {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setFailureReason("Payment processing interrupted");
        }
        
        Payment finalPayment = paymentRepository.save(payment);
        
        // Send payment status updated event to Kafka
        if (finalPayment.getStatus() == Payment.PaymentStatus.COMPLETED) {
            kafkaTemplate.send("payment-events", "payment-completed", finalPayment);
        } else {
            kafkaTemplate.send("payment-events", "payment-failed", finalPayment);
        }
        
        return finalPayment;
    }
    
    public Payment updatePaymentStatus(Long paymentId, Payment.PaymentStatus status) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));
        
        payment.setStatus(status);
        Payment updatedPayment = paymentRepository.save(payment);
        
        // Send payment status updated event to Kafka
        kafkaTemplate.send("payment-events", "payment-status-updated", updatedPayment);
        
        return updatedPayment;
    }
    
    public Payment refundPayment(Long paymentId, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));
        
        if (payment.getStatus() != Payment.PaymentStatus.COMPLETED) {
            throw new RuntimeException("Cannot refund payment that is not completed");
        }
        
        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        payment.setFailureReason(reason);
        Payment updatedPayment = paymentRepository.save(payment);
        
        // Send payment refunded event to Kafka
        kafkaTemplate.send("payment-events", "payment-refunded", updatedPayment);
        
        return updatedPayment;
    }
    
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
    }
    
    public Payment getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Payment not found with transaction id: " + transactionId));
    }
    
    public List<Payment> getPaymentsByUserId(Long userId) {
        return paymentRepository.findByUserId(userId);
    }
    
    public List<Payment> getPaymentsByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }
    
    public List<Payment> getPaymentsByStatus(Payment.PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }
    
    public List<Payment> getUserPaymentsByStatus(Long userId, Payment.PaymentStatus status) {
        return paymentRepository.findUserPaymentsByStatus(userId, status);
    }
    
    public List<Payment> getOrderPaymentsByStatus(Long orderId, Payment.PaymentStatus status) {
        return paymentRepository.findOrderPaymentsByStatus(orderId, status);
    }
    
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
    
    public boolean isPaymentCompleted(Long orderId) {
        List<Payment> payments = paymentRepository.findByOrderId(orderId);
        return payments.stream()
                .anyMatch(payment -> payment.getStatus() == Payment.PaymentStatus.COMPLETED);
    }
    
    public BigDecimal getTotalAmountPaid(Long orderId) {
        List<Payment> payments = paymentRepository.findByOrderId(orderId);
        return payments.stream()
                .filter(payment -> payment.getStatus() == Payment.PaymentStatus.COMPLETED)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

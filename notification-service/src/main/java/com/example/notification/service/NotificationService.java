package com.example.notification.service;

import com.example.notification.entity.Notification;
import com.example.notification.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    public Notification createNotification(Long userId, String title, String message, 
                                         Notification.NotificationType type, 
                                         Notification.NotificationChannel channel) {
        Notification notification = new Notification(userId, title, message, type, channel);
        notification.setStatus(Notification.NotificationStatus.PENDING);
        
        Notification savedNotification = notificationRepository.save(notification);
        
        // Send notification created event to Kafka
        kafkaTemplate.send("notification-events", "notification-created", savedNotification);
        
        return savedNotification;
    }
    
    public Notification sendNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));
        
        if (notification.getStatus() != Notification.NotificationStatus.PENDING) {
            throw new RuntimeException("Notification is not in pending status");
        }
        
        try {
            switch (notification.getChannel()) {
                case EMAIL:
                    sendEmailNotification(notification);
                    break;
                case SMS:
                    sendSmsNotification(notification);
                    break;
                case PUSH:
                    sendPushNotification(notification);
                    break;
                case IN_APP:
                    sendInAppNotification(notification);
                    break;
            }
            
            notification.setStatus(Notification.NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            
        } catch (Exception e) {
            notification.setStatus(Notification.NotificationStatus.FAILED);
            notification.setMetadata("Error: " + e.getMessage());
        }
        
        Notification updatedNotification = notificationRepository.save(notification);
        
        // Send notification status updated event to Kafka
        kafkaTemplate.send("notification-events", "notification-status-updated", updatedNotification);
        
        return updatedNotification;
    }
    
    private void sendEmailNotification(Notification notification) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(notification.getRecipient());
        message.setSubject(notification.getTitle());
        message.setText(notification.getMessage());
        
        mailSender.send(message);
    }
    
    private void sendSmsNotification(Notification notification) {
        // In a real implementation, this would integrate with SMS providers like Twilio
        System.out.println("SMS to " + notification.getRecipient() + ": " + notification.getMessage());
    }
    
    private void sendPushNotification(Notification notification) {
        // In a real implementation, this would integrate with push notification services
        System.out.println("Push notification to user " + notification.getUserId() + ": " + notification.getMessage());
    }
    
    private void sendInAppNotification(Notification notification) {
        // In a real implementation, this would store the notification for in-app display
        System.out.println("In-app notification for user " + notification.getUserId() + ": " + notification.getMessage());
    }
    
    public Notification updateNotificationStatus(Long notificationId, Notification.NotificationStatus status) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));
        
        notification.setStatus(status);
        if (status == Notification.NotificationStatus.SENT) {
            notification.setSentAt(LocalDateTime.now());
        }
        
        Notification updatedNotification = notificationRepository.save(notification);
        
        // Send notification status updated event to Kafka
        kafkaTemplate.send("notification-events", "notification-status-updated", updatedNotification);
        
        return updatedNotification;
    }
    
    public Notification getNotificationById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));
    }
    
    public List<Notification> getNotificationsByUserId(Long userId) {
        return notificationRepository.findUserNotificationsByDateDesc(userId);
    }
    
    public List<Notification> getNotificationsByStatus(Notification.NotificationStatus status) {
        return notificationRepository.findByStatus(status);
    }
    
    public List<Notification> getNotificationsByType(Notification.NotificationType type) {
        return notificationRepository.findByType(type);
    }
    
    public List<Notification> getUserNotificationsByStatus(Long userId, Notification.NotificationStatus status) {
        return notificationRepository.findUserNotificationsByStatus(userId, status);
    }
    
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }
    
    public Long getUnreadNotificationCount(Long userId) {
        return notificationRepository.countUserNotificationsByStatus(userId, Notification.NotificationStatus.PENDING);
    }
    
    // Kafka Event Listeners
    @KafkaListener(topics = "user-events", groupId = "notification-service-group")
    public void handleUserEvents(String event) {
        System.out.println("Received user event: " + event);
        // Handle user registration, password reset, etc.
    }
    
    @KafkaListener(topics = "order-events", groupId = "notification-service-group")
    public void handleOrderEvents(String event) {
        System.out.println("Received order event: " + event);
        // Handle order confirmation, shipping, delivery, etc.
    }
    
    @KafkaListener(topics = "payment-events", groupId = "notification-service-group")
    public void handlePaymentEvents(String event) {
        System.out.println("Received payment event: " + event);
        // Handle payment success, failure, refund, etc.
    }
    
    @KafkaListener(topics = "cart-events", groupId = "notification-service-group")
    public void handleCartEvents(String event) {
        System.out.println("Received cart event: " + event);
        // Handle cart abandonment, checkout reminders, etc.
    }
}

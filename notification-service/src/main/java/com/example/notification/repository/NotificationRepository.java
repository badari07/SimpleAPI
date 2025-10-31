package com.example.notification.repository;

import com.example.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByUserId(Long userId);
    
    Page<Notification> findByUserId(Long userId, Pageable pageable);
    
    List<Notification> findByStatus(Notification.NotificationStatus status);
    
    List<Notification> findByType(Notification.NotificationType type);
    
    List<Notification> findByChannel(Notification.NotificationChannel channel);
    
    List<Notification> findByUserIdAndStatus(Long userId, Notification.NotificationStatus status);
    
    List<Notification> findByUserIdAndType(Long userId, Notification.NotificationType type);
    
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId ORDER BY n.createdAt DESC")
    List<Notification> findUserNotificationsByDateDesc(@Param("userId") Long userId);
    
    @Query("SELECT n FROM Notification n WHERE n.status = :status AND n.createdAt BETWEEN :startDate AND :endDate")
    List<Notification> findNotificationsByStatusAndDateRange(@Param("status") Notification.NotificationStatus status, 
                                                           @Param("startDate") LocalDateTime startDate, 
                                                           @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.status = :status ORDER BY n.createdAt DESC")
    List<Notification> findUserNotificationsByStatus(@Param("userId") Long userId, 
                                                    @Param("status") Notification.NotificationStatus status);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.status = :status")
    Long countUserNotificationsByStatus(@Param("userId") Long userId, 
                                       @Param("status") Notification.NotificationStatus status);
}

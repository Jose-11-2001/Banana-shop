package com.bananashop.service;

import com.bananashop.model.Notification;
import com.bananashop.model.User;
import com.bananashop.repository.NotificationRepository;
import com.bananashop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    // Create notification for specific user
    @Transactional
    public Notification createNotification(User user, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        
        Notification savedNotification = notificationRepository.save(notification);
        
        // Send real-time WebSocket notification
        Map<String, Object> wsNotification = new HashMap<>();
        wsNotification.put("id", savedNotification.getId());
        wsNotification.put("message", message);
        wsNotification.put("createdAt", savedNotification.getCreatedAt());
        
        messagingTemplate.convertAndSendToUser(
            user.getEmail(),
            "/topic/notifications",
            wsNotification
        );
        
        return savedNotification;
    }
    
    // Create notification for all admins
    @Transactional
    public void createNotificationForAdmins(String message) {
        List<User> admins = userRepository.findByRole(User.Role.ADMIN);
        for (User admin : admins) {
            createNotification(admin, message);
        }
    }
    
    // Get user notifications
    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    // Mark notification as read
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }
    
    // Mark all as read for user
    @Transactional
    public void markAllAsRead(User user) {
        List<Notification> notifications = notificationRepository.findByUserAndIsReadFalse(user);
        for (Notification notification : notifications) {
            notification.setIsRead(true);
        }
        notificationRepository.saveAll(notifications);
    }
    
    // Get unread count
    public long getUnreadCount(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }
}
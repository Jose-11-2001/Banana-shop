
package com.example.Bananashop.service;

import com.example.Bananashop.model.Notification;
import com.example.Bananashop.model.Order;
import com.example.Bananashop.model.User;
import com.example.Bananashop.repository.NotificationRepository;
import com.example.Bananashop.repository.UserRepository;
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
    
    @Autowired
    private EmailService emailService;
    
    // Create notification for specific user
    @Transactional
    public Notification createNotification(User user, String message, String type, String referenceId) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setType(type);
        notification.setReferenceId(referenceId);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        
        Notification savedNotification = notificationRepository.save(notification);
        
        // Send real-time WebSocket notification
        Map<String, Object> wsNotification = new HashMap<>();
        wsNotification.put("id", savedNotification.getId());
        wsNotification.put("message", message);
        wsNotification.put("type", type);
        wsNotification.put("referenceId", referenceId);
        wsNotification.put("createdAt", savedNotification.getCreatedAt());
        
        messagingTemplate.convertAndSendToUser(
            user.getEmail(),
            "/topic/notifications",
            wsNotification
        );
        
        return savedNotification;
    }
    
    // Notify all admins about new order
    @Transactional
    public void notifyAdminsNewOrder(Order order) {
        List<User> admins = userRepository.findByRole(User.Role.ADMIN);
        String message = "🛒 New order #" + order.getId() + " from " + order.getCustomerName() + " - $" + order.getTotalAmount();
        
        for (User admin : admins) {
            createNotification(admin, message, "NEW_ORDER", String.valueOf(order.getId()));
            
            // Also send email to admin
            emailService.sendEmail(
                admin.getEmail(),
                "New Order Received!",
                "Order #" + order.getId() + " from " + order.getCustomerName() + " requires your attention.\n\nTotal Amount: $" + order.getTotalAmount()
            );
        }
    }
    
    // Notify customer when order status changes
    @Transactional
    public void notifyCustomerOrderStatus(Order order, String status, String rejectionReason) {
        String message;
        String type = "ORDER_UPDATE";
        
        if (status.equals("APPROVED")) {
            message = "✅ Your order #" + order.getId() + " has been approved! Your items will be delivered soon.";
        } else if (status.equals("REJECTED")) {
            message = "❌ Your order #" + order.getId() + " has been rejected. Reason: " + rejectionReason;
        } else {
            message = "📦 Your order #" + order.getId() + " is now " + status.toLowerCase();
        }
        
        createNotification(order.getCustomer(), message, type, String.valueOf(order.getId()));
        
        // Also send email
        emailService.sendOrderStatusUpdate(
            order.getCustomerEmail(),
            order.getCustomerName(),
            order.getId(),
            status,
            rejectionReason
        );
    }
    
    // Get user notifications
    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    // Get unread count for user
    public long getUnreadCount(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
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
}
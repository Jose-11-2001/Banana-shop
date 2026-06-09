
package com.bananashop.repository;

import com.bananashop.model.Notification;
import com.bananashop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Find notifications by user, ordered by creation date
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    
    // Find unread notifications for a user
    List<Notification> findByUserAndIsReadFalse(User user);
    
    // Count unread notifications for a user
    long countByUserAndIsReadFalse(User user);
}
package com.bananashop.service;

import com.bananashop.dto.OrderDTO;
import com.bananashop.model.*;
import com.bananashop.repository.OrderRepository;
import com.bananashop.repository.ProductRepository;
import com.bananashop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private NotificationService notificationService;
    
    // Create new order
    @Transactional
    public Order createOrder(String userEmail, OrderDTO orderDTO) {
        User customer = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Order order = new Order();
        order.setCustomer(customer);
        order.setCustomerName(customer.getName());
        order.setCustomerEmail(customer.getEmail());
        order.setDeliveryAddress(orderDTO.getDeliveryAddress());
        
        double totalAmount = 0;
        List<OrderItem> items = new ArrayList<>();
        
        for (OrderDTO.OrderItemDTO itemDTO : orderDTO.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found: " + itemDTO.getProductId()));
            
            // Check stock
            if (product.getStock() < itemDTO.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }
            
            // Update stock
            product.setStock(product.getStock() - itemDTO.getQuantity());
            productRepository.save(product);
            
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setProductName(product.getName());
            item.setProductImage(product.getImages().isEmpty() ? null : product.getImages().get(0).getImageUrl());
            item.setQuantity(itemDTO.getQuantity());
            item.setPrice(product.getPrice());
            
            items.add(item);
            totalAmount += product.getPrice() * itemDTO.getQuantity();
        }
        
        order.setItems(items);
        order.setTotalAmount(totalAmount);
        
        Order savedOrder = orderRepository.save(order);
        
        // Send notification to admin via WebSocket
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "NEW_ORDER");
        notification.put("orderId", savedOrder.getId());
        notification.put("customerName", customer.getName());
        notification.put("totalAmount", totalAmount);
        notification.put("timestamp", LocalDateTime.now());
        
        messagingTemplate.convertAndSend("/topic/admin/orders", notification);
        
        // Create notification in database for admin
        notificationService.createNotificationForAdmins(
            "New order #" + savedOrder.getId() + " from " + customer.getName()
        );
        
        return savedOrder;
    }
    
    // Get order by ID
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found"));
    }
    
    // Get customer orders
    public List<Order> getCustomerOrders(String userEmail) {
        User customer = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return orderRepository.findByCustomerOrderByCreatedAtDesc(customer);
    }
    
    // Get all orders (admin)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
    
    // Get orders by status (admin)
    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status);
    }
    
    // Update order status (admin approval/rejection)
    @Transactional
    public Order updateOrderStatus(Long orderId, Order.OrderStatus status, String rejectionReason) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        
        Order.OrderStatus previousStatus = order.getStatus();
        order.setStatus(status);
        
        if (status == Order.OrderStatus.REJECTED && rejectionReason != null) {
            order.setRejectionReason(rejectionReason);
            
            // Restore stock if order is rejected
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            }
        }
        
        Order updatedOrder = orderRepository.save(order);
        
        // Send WebSocket notification to customer
        Map<String, Object> customerNotification = new HashMap<>();
        customerNotification.put("type", "ORDER_STATUS_UPDATE");
        customerNotification.put("orderId", orderId);
        customerNotification.put("status", status.toString());
        customerNotification.put("rejectionReason", rejectionReason);
        
        messagingTemplate.convertAndSendToUser(
            order.getCustomer().getEmail(),
            "/topic/orders",
            customerNotification
        );
        
        // Send email notification
        String subject = "Order #" + orderId + " Status Update";
        String content;
        
        if (status == Order.OrderStatus.APPROVED) {
            content = "Great news! Your order #" + orderId + " has been approved and is being processed.";
        } else {
            content = "We regret to inform you that your order #" + orderId + " has been rejected.\n" +
                      "Reason: " + rejectionReason;
        }
        
        emailService.sendEmail(order.getCustomerEmail(), subject, content);
        
        // Create in-app notification
        notificationService.createNotification(
            order.getCustomer(),
            "Order #" + orderId + " is now " + status.toString().toLowerCase()
        );
        
        return updatedOrder;
    }
    
    // Get order statistics for admin dashboard
    public Map<String, Object> getOrderStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalOrders", orderRepository.count());
        stats.put("pendingOrders", orderRepository.countByStatus(Order.OrderStatus.PENDING));
        stats.put("approvedOrders", orderRepository.countByStatus(Order.OrderStatus.APPROVED));
        stats.put("rejectedOrders", orderRepository.countByStatus(Order.OrderStatus.REJECTED));
        stats.put("totalSales", orderRepository.getTotalSales() != null ? orderRepository.getTotalSales() : 0.0);
        
        return stats;
    }
}
package com.example.Bananashop.controller;

import com.example.Bananashop.dto.OrderDTO;
import com.example.Bananashop.dto.OrderStatusUpdateDTO;
import com.example.Bananashop.model.Order;
import com.example.Bananashop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    // ✅ Create new order (customer)
    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(
            Authentication authentication,
            @RequestBody OrderDTO orderDTO) {
        try {
            // ✅ Get email from authentication
            String email = authentication.getName();
            System.out.println("📤 Creating order for user: " + email);
            
            Order order = orderService.createOrder(email, orderDTO);
            System.out.println("✅ Order created with ID: " + order.getId());
            
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            System.err.println("❌ Error creating order: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to create order", "message", e.getMessage()));
        }
    }
    
    // ✅ Get customer's orders
    @GetMapping("/customer/orders")
    public ResponseEntity<List<Order>> getCustomerOrders(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<Order> orders = orderService.getCustomerOrders(userDetails.getUsername());
        return ResponseEntity.ok(orders);
    }
    
    // ✅ Get order by ID
    @GetMapping("/orders/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }
    
    // ✅ Admin endpoints
    @GetMapping("/admin/orders")
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }
    
    @GetMapping("/admin/orders/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable Order.OrderStatus status) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }
    
    @PutMapping("/admin/orders/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody OrderStatusUpdateDTO updateDTO) {
        Order order = orderService.updateOrderStatus(id, updateDTO.getStatus(), updateDTO.getRejectionReason());
        return ResponseEntity.ok(order);
    }
    
    @GetMapping("/admin/orders/stats")
    public ResponseEntity<?> getOrderStatistics() {
        return ResponseEntity.ok(orderService.getOrderStatistics());
    }
}
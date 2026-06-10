package com.example.Bananashop.controller;

import com.example.Bananashop.dto.OrderDTO;
import com.example.Bananashop.dto.OrderStatusUpdateDTO;
import com.example.Bananashop.model.Order;
import com.example.Bananashop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    // Create new order (customer)
    @PostMapping("/orders")
    public ResponseEntity<Order> createOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody OrderDTO orderDTO) {
        Order order = orderService.createOrder(userDetails.getUsername(), orderDTO);
        return ResponseEntity.ok(order);
    }
    
    // Get customer's orders
    @GetMapping("/customer/orders")
    public ResponseEntity<List<Order>> getCustomerOrders(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<Order> orders = orderService.getCustomerOrders(userDetails.getUsername());
        return ResponseEntity.ok(orders);
    }
    
    // Get order by ID (customer can view their own orders)
    @GetMapping("/orders/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }
    
    // Admin endpoints
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
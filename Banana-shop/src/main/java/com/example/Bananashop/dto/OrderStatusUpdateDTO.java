package com.example.Bananashop.dto;

import com.example.Bananashop.model.Order;

public class OrderStatusUpdateDTO {
    private Order.OrderStatus status;
    private String rejectionReason;
    
    public OrderStatusUpdateDTO() {}
    
    public Order.OrderStatus getStatus() { return status; }
    public void setStatus(Order.OrderStatus status) { this.status = status; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}
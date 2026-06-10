package com.bananashop.dto;

import com.bananashop.model.Order;

public class OrderStatusUpdateDTO {
    private Order.OrderStatus status;
    private String rejectionReason;
    
    public OrderStatusUpdateDTO() {}
    
    public Order.OrderStatus getStatus() { return status; }
    public void setStatus(Order.OrderStatus status) { this.status = status; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}
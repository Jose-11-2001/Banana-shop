package com.bananashop.dto;

import lombok.Data;
import com.bananashop.model.Order;

@Data
public class OrderStatusUpdateDTO {
    private Order.OrderStatus status;
    private String rejectionReason;
}
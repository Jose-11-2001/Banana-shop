package com.bananashop.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderDTO {
    private List<OrderItemDTO> items;
    private String deliveryAddress;
    
    @Data
    public static class OrderItemDTO {
        private Long productId;
        private Integer quantity;
    }
}

@Data
class OrderItemDTO {
    private Long productId;
    private Integer quantity;
}

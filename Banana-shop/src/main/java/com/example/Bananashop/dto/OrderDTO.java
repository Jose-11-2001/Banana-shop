package com.example.Bananashop.dto;

import java.util.List;

public class OrderDTO {
    private List<OrderItemDTO> items;
    private String deliveryAddress;
    
    public OrderDTO() {}
    
    public List<OrderItemDTO> getItems() { return items; }
    public void setItems(List<OrderItemDTO> items) { this.items = items; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    
    public static class OrderItemDTO {
        private Long productId;
        private Integer quantity;
        
        public OrderItemDTO() {}
        
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
}
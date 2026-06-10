package com.example.Bananashop.repository;

import com.example.Bananashop.model.OrderItem;
import com.example.Bananashop.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrder(Order order);
    List<OrderItem> findByProductId(Long productId);
}
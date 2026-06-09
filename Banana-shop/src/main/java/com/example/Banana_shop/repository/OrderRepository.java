package com.bananashop.repository;

import com.bananashop.model.Order;
import com.bananashop.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Find orders by customer
    List<Order> findByCustomerOrderByCreatedAtDesc(User customer);
    Page<Order> findByCustomer(User customer, Pageable pageable);
    
    // Find orders by status
    List<Order> findByStatus(Order.OrderStatus status);
    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);
    
    // Find orders by date range
    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    // Count orders by status for dashboard
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    long countByStatus(@Param("status") Order.OrderStatus status);
    
    // Get total sales
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'APPROVED'")
    Double getTotalSales();
    
    // Get monthly sales
    @Query("SELECT FUNCTION('MONTH', o.createdAt), SUM(o.totalAmount) FROM Order o " +
           "WHERE o.status = 'APPROVED' AND YEAR(o.createdAt) = :year " +
           "GROUP BY FUNCTION('MONTH', o.createdAt)")
    List<Object[]> getMonthlySales(@Param("year") int year);
}
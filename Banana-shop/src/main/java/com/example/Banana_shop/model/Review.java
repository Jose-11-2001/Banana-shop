package com.bananashop.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Data
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
    
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private User customer;
    
    private Integer rating;
    private String comment;
    
    @Enumerated(EnumType.STRING)
    private ReviewStatus status;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        status = ReviewStatus.PENDING;
    }
    
    public enum ReviewStatus {
        PENDING, APPROVED, REJECTED
    }
}
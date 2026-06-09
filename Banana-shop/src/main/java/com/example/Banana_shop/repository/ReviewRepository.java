package com.bananashop.repository;

import com.bananashop.model.Product;
import com.bananashop.model.Review;
import com.bananashop.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    // Find reviews by product (only approved)
    List<Review> findByProductAndStatus(Product product, Review.ReviewStatus status);
    
    // Find pending reviews for admin
    List<Review> findByStatus(Review.ReviewStatus status);
    Page<Review> findByStatus(Review.ReviewStatus status, Pageable pageable);
    
    // Find review by user and product
    Optional<Review> findByCustomerAndProduct(User customer, Product product);
    
    // Check if user has already reviewed product
    boolean existsByCustomerAndProduct(User customer, Product product);
    
    // Calculate average rating for product
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product = :product AND r.status = 'APPROVED'")
    Double getAverageRatingForProduct(@Param("product") Product product);
    
    // Get total reviews count for product
    @Query("SELECT COUNT(r) FROM Review r WHERE r.product = :product AND r.status = 'APPROVED'")
    Long getTotalReviewsForProduct(@Param("product") Product product);
    
    // Get reviews by status count
    @Query("SELECT COUNT(r) FROM Review r WHERE r.status = :status")
    long countByStatus(@Param("status") Review.ReviewStatus status);
}
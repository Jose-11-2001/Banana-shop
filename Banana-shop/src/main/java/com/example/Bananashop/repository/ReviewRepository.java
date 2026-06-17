package com.example.Bananashop.repository;

import com.example.Bananashop.model.Product;
import com.example.Bananashop.model.Review;
import com.example.Bananashop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    List<Review> findByProductAndStatus(Product product, Review.ReviewStatus status);
    
    List<Review> findByStatus(Review.ReviewStatus status);
    
    boolean existsByCustomerAndProduct(User customer, Product product);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product = :product AND r.status = 'APPROVED'")
    Double getAverageRatingForProduct(@Param("product") Product product);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.product = :product AND r.status = 'APPROVED'")
    Long getTotalReviewsForProduct(@Param("product") Product product);
    
    long countByStatus(Review.ReviewStatus status);
    
    // ✅ Add this method
    List<Review> findByProductIdAndStatus(Long productId, Review.ReviewStatus status);
}
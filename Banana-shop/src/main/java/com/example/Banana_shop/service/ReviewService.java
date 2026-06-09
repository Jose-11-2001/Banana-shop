package com.bananashop.service;

import com.bananashop.model.*;
import com.bananashop.repository.ProductRepository;
import com.bananashop.repository.ReviewRepository;
import com.bananashop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReviewService {
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private NotificationService notificationService;
    
    // Add review (customer)
    @Transactional
    public Review addReview(String userEmail, Long productId, Integer rating, String comment) {
        User customer = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // Check if user already reviewed this product
        if (reviewRepository.existsByCustomerAndProduct(customer, product)) {
            throw new RuntimeException("You have already reviewed this product");
        }
        
        Review review = new Review();
        review.setCustomer(customer);
        review.setProduct(product);
        review.setRating(rating);
        review.setComment(comment);
        review.setStatus(Review.ReviewStatus.PENDING);
        review.setCreatedAt(LocalDateTime.now());
        
        Review savedReview = reviewRepository.save(review);
        
        // Notify admin about new review
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "NEW_REVIEW");
        notification.put("reviewId", savedReview.getId());
        notification.put("productName", product.getName());
        notification.put("customerName", customer.getName());
        
        messagingTemplate.convertAndSend("/topic/admin/reviews", notification);
        
        return savedReview;
    }
    
    // Get approved reviews for product
    public List<Review> getProductReviews(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        return reviewRepository.findByProductAndStatus(product, Review.ReviewStatus.APPROVED);
    }
    
    // Get all pending reviews (admin)
    public List<Review> getPendingReviews() {
        return reviewRepository.findByStatus(Review.ReviewStatus.PENDING);
    }
    
    // Moderate review (admin approve/reject)
    @Transactional
    public Review moderateReview(Long reviewId, Review.ReviewStatus status) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new RuntimeException("Review not found"));
        
        review.setStatus(status);
        Review moderatedReview = reviewRepository.save(review);
        
        // Update product average rating if review is approved
        if (status == Review.ReviewStatus.APPROVED) {
            updateProductRating(review.getProduct());
        }
        
        // Notify customer about review moderation
        String subject = "Your Review for " + review.getProduct().getName();
        String content;
        
        if (status == Review.ReviewStatus.APPROVED) {
            content = "Great news! Your review has been approved and is now visible on the product page.";
        } else {
            content = "We regret to inform you that your review has been rejected as it didn't meet our guidelines.";
        }
        
        emailService.sendEmail(review.getCustomer().getEmail(), subject, content);
        
        // Send WebSocket notification to customer
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "REVIEW_MODERATED");
        notification.put("reviewId", reviewId);
        notification.put("status", status.toString());
        notification.put("productName", review.getProduct().getName());
        
        messagingTemplate.convertAndSendToUser(
            review.getCustomer().getEmail(),
            "/topic/reviews",
            notification
        );
        
        return moderatedReview;
    }
    
    // Update product average rating
    private void updateProductRating(Product product) {
        Double averageRating = reviewRepository.getAverageRatingForProduct(product);
        Long totalReviews = reviewRepository.getTotalReviewsForProduct(product);
        
        product.setAverageRating(averageRating != null ? averageRating : 0.0);
        product.setTotalReviews(totalReviews != null ? totalReviews : 0L);
        
        productRepository.save(product);
    }
    
    // Get review statistics for admin
    public Map<String, Object> getReviewStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("pendingReviews", reviewRepository.countByStatus(Review.ReviewStatus.PENDING));
        stats.put("approvedReviews", reviewRepository.countByStatus(Review.ReviewStatus.APPROVED));
        stats.put("rejectedReviews", reviewRepository.countByStatus(Review.ReviewStatus.REJECTED));
        
        return stats;
    }
    
    // Delete review (admin)
    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new RuntimeException("Review not found"));
        
        reviewRepository.delete(review);
        updateProductRating(review.getProduct());
    }
}
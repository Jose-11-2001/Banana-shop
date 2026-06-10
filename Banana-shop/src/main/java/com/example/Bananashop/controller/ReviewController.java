package com.example.Bananashop.controller;

import com.example.Bananashop.dto.ReviewDTO;
import com.example.Bananashop.model.Review;
import com.example.Bananashop.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class ReviewController {
    
    @Autowired
    private ReviewService reviewService;
    
    // Add review (customer)
    @PostMapping("/products/{productId}/reviews")
    public ResponseEntity<Review> addReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId,
            @RequestBody ReviewDTO reviewDTO) {
        Review review = reviewService.addReview(
            userDetails.getUsername(),
            productId,
            reviewDTO.getRating(),
            reviewDTO.getComment()
        );
        return ResponseEntity.ok(review);
    }
    
    // Get product reviews (public - only approved)
    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<List<Review>> getProductReviews(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getProductReviews(productId));
    }
    
    // Admin endpoints
    @GetMapping("/admin/reviews/pending")
    public ResponseEntity<List<Review>> getPendingReviews() {
        return ResponseEntity.ok(reviewService.getPendingReviews());
    }
    
    @PutMapping("/admin/reviews/{id}/status")
    public ResponseEntity<Review> moderateReview(
            @PathVariable Long id,
            @RequestParam Review.ReviewStatus status) {
        return ResponseEntity.ok(reviewService.moderateReview(id, status));
    }
    
    @DeleteMapping("/admin/reviews/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/admin/reviews/stats")
    public ResponseEntity<?> getReviewStatistics() {
        return ResponseEntity.ok(reviewService.getReviewStatistics());
    }
}
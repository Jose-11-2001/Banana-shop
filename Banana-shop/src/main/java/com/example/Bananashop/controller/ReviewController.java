package com.example.Bananashop.controller;

import com.example.Bananashop.dto.ReviewDTO;
import com.example.Bananashop.model.Review;
import com.example.Bananashop.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class ReviewController {
    
    @Autowired
    private ReviewService reviewService;
    
    // ✅ Add review (customer)
    @PostMapping("/products/{productId}/reviews")
    public ResponseEntity<?> addReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId,
            @RequestBody ReviewDTO reviewDTO) {
        try {
            System.out.println("📝 Adding review for product: " + productId + " by user: " + userDetails.getUsername());
            Review review = reviewService.addReview(
                userDetails.getUsername(),
                productId,
                reviewDTO.getRating(),
                reviewDTO.getComment()
            );
            System.out.println("✅ Review added with ID: " + review.getId());
            return ResponseEntity.ok(review);
        } catch (Exception e) {
            System.err.println("❌ Error adding review: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to add review", "message", e.getMessage()));
        }
    }
    
    // ✅ Get product reviews (public - only approved)
    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<List<Review>> getProductReviews(@PathVariable Long productId) {
        System.out.println("📥 Fetching reviews for product: " + productId);
        List<Review> reviews = reviewService.getProductReviews(productId);
        System.out.println("✅ Found " + reviews.size() + " reviews");
        return ResponseEntity.ok(reviews);
    }
    
    // ✅ Admin - Get all pending reviews
    @GetMapping("/admin/reviews/pending")
    public ResponseEntity<List<Review>> getPendingReviews() {
        System.out.println("📥 Admin fetching pending reviews");
        List<Review> reviews = reviewService.getPendingReviews();
        System.out.println("✅ Found " + reviews.size() + " pending reviews");
        return ResponseEntity.ok(reviews);
    }
    
    // ✅ Admin - Update review status
    @PutMapping("/admin/reviews/{id}/status")
    public ResponseEntity<?> moderateReview(
            @PathVariable Long id,
            @RequestParam Review.ReviewStatus status) {
        try {
            System.out.println("📤 Admin moderating review: " + id + " to status: " + status);
            Review updated = reviewService.moderateReview(id, status);
            System.out.println("✅ Review moderated");
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            System.err.println("❌ Error moderating review: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to moderate review", "message", e.getMessage()));
        }
    }
    
    // ✅ Admin - Delete review
    @DeleteMapping("/admin/reviews/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable Long id) {
        System.out.println("🗑️ Admin deleting review: " + id);
        reviewService.deleteReview(id);
        System.out.println("✅ Review deleted");
        return ResponseEntity.ok().build();
    }
    
    // ✅ Admin - Get review stats
    @GetMapping("/admin/reviews/stats")
    public ResponseEntity<?> getReviewStats() {
        System.out.println("📥 Admin fetching review stats");
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("pendingReviews", reviewService.countPendingReviews());
            System.out.println("✅ Pending reviews: " + stats.get("pendingReviews"));
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("❌ Error fetching review stats: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch review stats", "message", e.getMessage()));
        }
    }
}
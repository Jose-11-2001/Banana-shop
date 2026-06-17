package com.example.Bananashop.controller;

import com.example.Bananashop.dto.ForgotPasswordRequest;
import com.example.Bananashop.dto.ResetPasswordRequest;
import com.example.Bananashop.model.User;
import com.example.Bananashop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("📥 Fetching profile for: " + userDetails.getUsername());
            User user = userService.findByEmail(userDetails.getUsername());
            System.out.println("✅ Profile fetched for: " + user.getEmail());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            System.err.println("❌ Error fetching profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch profile", "message", e.getMessage()));
        }
    }
    
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody User updatedUser) {
        try {
            System.out.println("📤 Updating profile for: " + userDetails.getUsername());
            System.out.println("   New name: " + updatedUser.getName());
            System.out.println("   New location: " + updatedUser.getLocation());
            
            // ✅ Don't update email - it should be read-only
            User user = userService.updateProfile(userDetails.getUsername(), updatedUser);
            System.out.println("✅ Profile updated for: " + user.getEmail());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            System.err.println("❌ Error updating profile: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to update profile", "message", e.getMessage()));
        }
    }
    
    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("avatar") MultipartFile file) {
        try {
            String avatarUrl = userService.uploadAvatar(userDetails.getUsername(), file);
            return ResponseEntity.ok().body(Map.of("avatarUrl", avatarUrl));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to upload avatar", "message", e.getMessage()));
        }
    }
    
    @DeleteMapping("/account")
    public ResponseEntity<?> deleteAccount(@AuthenticationPrincipal UserDetails userDetails) {
        userService.deleteAccount(userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
    
    // ✅ Admin - Get user stats
    @GetMapping("/admin/users/stats")
    public ResponseEntity<?> getUserStats() {
        System.out.println("📥 Admin fetching user stats");
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", userService.getTotalUsers());
            System.out.println("✅ Total users: " + stats.get("totalUsers"));
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("❌ Error fetching user stats: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch user stats", "message", e.getMessage()));
        }
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        userService.forgotPassword(request.getEmail());
        return ResponseEntity.ok().body(Map.of("message", "Password reset email sent successfully"));
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok().body(Map.of("message", "Password reset successfully"));
    }
}
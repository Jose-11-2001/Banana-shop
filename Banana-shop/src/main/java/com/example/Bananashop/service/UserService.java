package com.example.Bananashop.service;

import com.example.Bananashop.model.User;
import com.example.Bananashop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private EmailService emailService;
    
    @Value("${upload.path}")
    private String uploadPath;
    
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;
    
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }
    
    @Transactional
    public User register(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        
        // Auto-set admin role for specific email
        if (user.getEmail().equals("adminbananashop@gmail.com")) {
            user.setRole(User.Role.ADMIN);
        } else if (user.getRole() == null) {
            user.setRole(User.Role.CUSTOMER);
        }
        
        return userRepository.save(user);
    }
    
    @Transactional
    public User updateProfile(String email, User updatedUser) {
        System.out.println("📝 Updating profile for: " + email);
        
        User user = findByEmail(email);
        System.out.println("👤 Found user: " + user.getEmail());
        System.out.println("   Old name: " + user.getName());
        System.out.println("   New name: " + updatedUser.getName());
        System.out.println("   Old location: " + user.getLocation());
        System.out.println("   New location: " + updatedUser.getLocation());
        
        // ✅ Only update name and location (email should NOT be updated)
        user.setName(updatedUser.getName());
        user.setLocation(updatedUser.getLocation());
        // DO NOT update email - it should be immutable
        
        User savedUser = userRepository.save(user);
        System.out.println("✅ Profile updated for: " + savedUser.getEmail());
        
        return savedUser;
    }
    
    @Transactional
    public String uploadAvatar(String email, MultipartFile file) {
        try {
            User user = findByEmail(email);
            
            Path uploadDir = Paths.get(uploadPath, "avatars");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            
            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath);
            
            String avatarUrl = "/uploads/avatars/" + filename;
            user.setAvatar(avatarUrl);
            userRepository.save(user);
            
            return avatarUrl;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload avatar", e);
        }
    }
    
    @Transactional
    public void deleteAccount(String email) {
        User user = findByEmail(email);
        userRepository.delete(user);
    }
    
    public long getTotalUsers() {
        return userRepository.count();
    }
    
    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);
        
        emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
    }
    
    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
            .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));
        
        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token has expired");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }
}
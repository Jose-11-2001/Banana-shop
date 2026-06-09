// service/UserService.java
package com.bananashop.service;

import com.bananashop.model.User;
import com.bananashop.repository.UserRepository;
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
import java.util.UUID;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Value("${upload.path}")
    private String uploadPath;
    
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    @Transactional
    public User register(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(User.Role.CUSTOMER);
        
        return userRepository.save(user);
    }
    
    @Transactional
    public User updateProfile(String email, User updatedUser) {
        User user = findByEmail(email);
        user.setName(updatedUser.getName());
        user.setLocation(updatedUser.getLocation());
        user.setEmail(updatedUser.getEmail());
        
        return userRepository.save(user);
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
}
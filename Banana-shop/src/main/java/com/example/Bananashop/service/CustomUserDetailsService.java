package com.example.Bananashop.service;

import com.example.Bananashop.model.User;
import com.example.Bananashop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("🔍 Loading user by email: " + email);
        
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> {
                System.out.println("❌ User not found: " + email);
                return new UsernameNotFoundException("User not found with email: " + email);
            });
        
        System.out.println("✅ User loaded: " + user.getEmail() + " with role: " + user.getRole());
        
        // ✅ Create authority with ROLE_ prefix
        String role = "ROLE_" + user.getRole().name();
        System.out.println("✅ Granted authority: " + role);
        
        return new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getPassword(),
            Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }
}
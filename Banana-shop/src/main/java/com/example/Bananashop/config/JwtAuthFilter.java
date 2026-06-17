package com.example.Bananashop.config;

import com.example.Bananashop.service.CustomUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;

    // ✅ List of public endpoints (both with and without /api)
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
        // Auth endpoints
        "/auth/login",
        "/auth/register",
        "/api/auth/login",
        "/api/auth/register",
        // Forgot/Reset password endpoints
        "/auth/forgot-password",
        "/api/auth/forgot-password",
        "/user/forgot-password",
        "/api/user/forgot-password",
        "/auth/reset-password",
        "/api/auth/reset-password",
        "/user/reset-password",
        "/api/user/reset-password",
        // Product endpoints
        "/products",
        "/api/products",
        // Swagger
        "/swagger-ui",
        "/api-docs",
        "/v3/api-docs",
        "/ws"
    );
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String path = request.getServletPath();
        String method = request.getMethod();
        
        System.out.println("🔍 JWT Filter - Path: " + path + ", Method: " + method);
        
        // ✅ Check if path is public
        boolean isPublic = PUBLIC_PATHS.stream().anyMatch(path::startsWith);
        
        if (isPublic) {
            System.out.println("🔓 Public endpoint: " + path + " - Skipping JWT filter");
            filterChain.doFilter(request, response);
            return;
        }
        
        final String authHeader = request.getHeader("Authorization");
        System.out.println("🔑 Auth Header: " + (authHeader != null ? "Present" : "Missing"));
        
        // ✅ Check for Authorization header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("⚠️ No Bearer token found for: " + path);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Authentication required\", \"message\": \"Missing or invalid token\"}");
            return;
        }
        
        final String jwt = authHeader.substring(7);
        System.out.println("🔐 Validating token for: " + path);
        System.out.println("🔐 Token: " + jwt.substring(0, 30) + "...");
        
        try {
            final String email = jwtService.extractEmail(jwt);
            System.out.println("📧 Extracted email: " + email);
            
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                System.out.println("👤 Loaded user: " + userDetails.getUsername());
                System.out.println("👤 User authorities: " + userDetails.getAuthorities());
                
                if (jwtService.validateToken(jwt, userDetails.getUsername())) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("✅ Authenticated user: " + email + " with authorities: " + userDetails.getAuthorities());
                } else {
                    System.out.println("❌ Token validation failed for: " + email);
                }
            }
            filterChain.doFilter(request, response);
            
        } catch (ExpiredJwtException e) {
            System.out.println("❌ Token expired: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Token expired\", \"message\": \"Please login again\", \"code\": \"TOKEN_EXPIRED\"}");
            return;
            
        } catch (MalformedJwtException | SignatureException e) {
            System.out.println("❌ Invalid token: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid token\", \"message\": \"Authentication failed\"}");
            return;
            
        } catch (Exception e) {
            System.out.println("❌ Authentication error: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Authentication failed\", \"message\": \"" + e.getMessage() + "\"}");
            return;
        }
    }
}
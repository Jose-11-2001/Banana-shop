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

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        
        // ✅ Get the request path
        String path = request.getServletPath();
        String method = request.getMethod();
        
        System.out.println("🔍 JWT Filter - Path: " + path + ", Method: " + method);
        
        // ✅ SKIP JWT FILTER FOR PUBLIC ENDPOINTS
        if (path.equals("/api/auth/login") || 
            path.equals("/api/auth/register") ||
            path.startsWith("/api/auth/") || 
            path.startsWith("/api/products") || 
            path.startsWith("/swagger-ui") || 
            path.startsWith("/api-docs") ||
            path.startsWith("/v3/api-docs") ||
            path.startsWith("/ws/") ||
            path.startsWith("/api/notifications/") ||
            path.equals("/api/user/forgot-password") ||
            path.equals("/api/user/reset-password")) {
            
            System.out.println("🔓 Public endpoint: " + path + " - Skipping JWT filter");
            filterChain.doFilter(request, response);
            return;
        }
        
        // ✅ Check for Authorization header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("⚠️ No Bearer token found for: " + path);
            filterChain.doFilter(request, response);
            return;
        }
        
        final String jwt = authHeader.substring(7);
        System.out.println("🔐 Validating token for: " + path);
        
        try {
            final String email = jwtService.extractEmail(jwt);
            System.out.println("📧 Extracted email: " + email);
            
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                System.out.println("👤 Loaded user: " + userDetails.getUsername());
                System.out.println("👤 User authorities: " + userDetails.getAuthorities());
                
                // ✅ Validate token with UserDetails
                if (jwtService.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("✅ Authenticated user: " + email + " with authorities: " + userDetails.getAuthorities());
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
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Authentication failed\", \"message\": \"" + e.getMessage() + "\"}");
            return;
        }
    }
}
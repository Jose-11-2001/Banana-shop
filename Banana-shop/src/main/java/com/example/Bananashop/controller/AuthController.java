package com.example.Bananashop.controller;

import com.example.Bananashop.config.JwtService;
import com.example.Bananashop.dto.LoginRequest;
import com.example.Bananashop.dto.AuthResponse;
import com.example.Bananashop.model.User;
import com.example.Bananashop.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
public class AuthController {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/login")
    @Operation(
        summary = "Login user", 
        description = "Authenticate user with email and password. Returns a JWT token for subsequent authorized requests."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully logged in",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(
                    name = "success",
                    value = """
                    {
                        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "user": {
                            "id": 1,
                            "email": "adminbananashop@gmail.com",
                            "name": "Admin User",
                            "role": "ADMIN",
                            "createdAt": "2026-01-01T00:00:00"
                        }
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Invalid email or password",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                    {
                        "timestamp": "2026-01-01T00:00:00",
                        "message": "Invalid email or password",
                        "status": 401
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid request format",
            content = @Content
        )
    })
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            System.out.println("🔐 Login attempt for: " + request.getEmail());
            
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            
            User user = userService.findByEmail(request.getEmail());
            
            // ✅ Generate token with role
            String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
            
            System.out.println("🔐 User logged in: " + user.getEmail() + " with role: " + user.getRole());
            System.out.println("🎫 Token generated with role: ROLE_" + user.getRole().name());
            
            return ResponseEntity.ok(new AuthResponse(token, user));
            
        } catch (Exception e) {
            System.out.println("❌ Login failed: " + e.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials", "message", e.getMessage()));
        }
    }
    
    @PostMapping("/register")
    @Operation(
        summary = "Register new user", 
        description = "Create a new customer account. Password must be at least 6 characters."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully registered",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = User.class),
                examples = @ExampleObject(
                    name = "success",
                    value = """
                    {
                        "id": 1,
                        "email": "adminbananashop@gmail.com",
                        "name": "Admin User",
                        "role": "ADMIN",
                        "createdAt": "2026-01-01T00:00:00"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Email already exists or invalid data",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                    {
                        "timestamp": "2026-01-01T00:00:00",
                        "message": "Email already registered",
                        "status": 400
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<?> register(@RequestBody User user) {
        // Log registration
        System.out.println(" Registering user: " + user.getEmail() + " with role: " + user.getRole());
        
        User registeredUser = userService.register(user);
        
        System.out.println(" User registered: " + registeredUser.getEmail() + " with role: " + registeredUser.getRole());
        
        return ResponseEntity.ok(registeredUser);
    }
}
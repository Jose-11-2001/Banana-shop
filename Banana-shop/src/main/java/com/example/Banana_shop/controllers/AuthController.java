package com.bananashop.controller;

import com.bananashop.config.JwtService;
import com.bananashop.dto.LoginRequest;
import com.bananashop.dto.AuthResponse;
import com.bananashop.model.User;
import com.bananashop.service.UserService;
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
                            "email": "user@example.com",
                            "name": "John Doe",
                            "role": "CUSTOMER",
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
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        
        String token = jwtService.generateToken(request.getEmail());
        User user = userService.findByEmail(request.getEmail());
        
        return ResponseEntity.ok(new AuthResponse(token, user));
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
                        "email": "newuser@example.com",
                        "name": "New User",
                        "role": "CUSTOMER",
                        "location": "New York",
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
        User registeredUser = userService.register(user);
        return ResponseEntity.ok(registeredUser);
    }
}
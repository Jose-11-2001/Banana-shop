package com.bananashop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Login request payload")
public class LoginRequest {
    
    @Schema(description = "User email address", example = "user@example.com", required = true)
    private String email;
    
    @Schema(description = "User password", example = "password123", required = true, minLength = 6)
    private String password;
    
    public LoginRequest() {}
    
    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
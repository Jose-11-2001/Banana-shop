package com.example.Bananashop.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.tags.Tag;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("🍌 Banana Shop API")
                .version("1.0.0")
                .description("""
                    ## Banana Shop E-Commerce Platform API
                    
                    This is the complete REST API documentation for the Banana Shop application.
                    
                    ### Features:
                    - User authentication (JWT)
                    - Product management
                    - Order processing
                    - Review system
                    - Real-time notifications
                    
                    ### Authentication
                    Use the `/api/auth/login` endpoint to get a JWT token.
                    Then click the **Authorize** button and enter: `Bearer YOUR_TOKEN_HERE`
                    """)
                .contact(new Contact()
                    .name("Banana Shop Support")
                    .email("support@bananashop.com")
                    .url("https://bananashop.com"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8080")
                    .description("Development Server"),
                new Server()
                    .url("https://api.bananashop.com")
                    .description("Production Server (coming soon)")
            ))
            .tags(Arrays.asList(
                new Tag().name("Authentication").description("Login and Registration endpoints"),
                new Tag().name("Products").description("Browse and search products"),
                new Tag().name("Orders").description("Place and track orders"),
                new Tag().name("Reviews").description("Product reviews and ratings"),
                new Tag().name("Admin").description("Administrative operations"),
                new Tag().name("User").description("User profile management")
            ))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
            .components(new Components()
                .addSecuritySchemes("Bearer Authentication", new SecurityScheme()
                    .name("Bearer Authentication")
                    .type(Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .in(In.HEADER)
                    .description("""
                        Enter JWT token in the format: **Bearer {token}**
                        
                        Example: `Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`
                        
                        Get your token from the `/api/auth/login` endpoint.
                        """)));
    }
}
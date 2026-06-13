package com.example.Bananashop.controller;

import com.example.Bananashop.model.Product;
import com.example.Bananashop.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.HashMap;
import java.util.Map; 

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Products", description = "Product management endpoints")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    @GetMapping("/products")
    @Operation(summary = "Get all products", description = "Retrieve list of all products (public)")
    public ResponseEntity<List<Product>> getAllProducts(
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(productService.getAllProducts(category));
    }
    
    @GetMapping("/products/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieve a single product by its ID")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }
    
    @PostMapping("/admin/products")
    @Operation(summary = "Create product", description = "Add a new product (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        return ResponseEntity.ok(productService.createProduct(product));
    }
    
    @PutMapping("/admin/products/{id}")
    @Operation(summary = "Update product", description = "Update existing product (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        return ResponseEntity.ok(productService.updateProduct(id, product));
    }
    
    @PostMapping("/admin/products/{id}/images")
    @Operation(summary = "Upload product image", description = "Add image to product (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> uploadProductImage(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile file) {
        productService.uploadProductImage(id, file);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/admin/products/images/{imageId}")
    @Operation(summary = "Delete product image", description = "Remove image from product (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> deleteProductImage(@PathVariable Long imageId) {
        productService.deleteProductImage(imageId);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/admin/products/stats")
    public ResponseEntity<?> getProductStats() {
    Map<String, Object> stats = new HashMap<>();
    stats.put("totalProducts", productService.getTotalProducts());
    return ResponseEntity.ok(stats);
}
}
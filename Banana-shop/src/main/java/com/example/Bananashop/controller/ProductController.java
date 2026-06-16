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
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Products", description = "Product management endpoints")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    // ✅ Public - Get all products
    @GetMapping("/products")
    @Operation(summary = "Get all products", description = "Retrieve list of all products (public)")
    public ResponseEntity<List<Product>> getAllProducts(
            @RequestParam(required = false) String category) {
        System.out.println("📥 Public - Fetching all products" + (category != null ? " with category: " + category : ""));
        List<Product> products = productService.getAllProducts(category);
        System.out.println("✅ Found " + products.size() + " products");
        return ResponseEntity.ok(products);
    }
    
    // ✅ Public - Get product by ID
    @GetMapping("/products/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieve a single product by its ID")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        System.out.println("📥 Public - Fetching product by ID: " + id);
        Product product = productService.getProductById(id);
        System.out.println("✅ Found product: " + product.getName());
        return ResponseEntity.ok(product);
    }
    
    // ✅ Admin - Get all products
    @GetMapping("/admin/products")
    @Operation(summary = "Get all products (admin)", description = "Retrieve list of all products with admin access")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<List<Product>> getAllProductsAdmin() {
        System.out.println("📥 Admin fetching all products");
        List<Product> products = productService.getAllProducts(null);
        System.out.println("✅ Found " + products.size() + " products");
        return ResponseEntity.ok(products);
    }
    
    // ✅ Admin - Create product
    @PostMapping("/admin/products")
    @Operation(summary = "Create product", description = "Add a new product (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        System.out.println("📤 Admin creating product: " + product.getName());
        System.out.println("   Category: " + product.getCategory());
        System.out.println("   Price: " + product.getPrice());
        System.out.println("   Stock: " + product.getStock());
        
        Product created = productService.createProduct(product);
        System.out.println("✅ Product created with ID: " + created.getId());
        return ResponseEntity.ok(created);
    }
    
    // ✅ Admin - Update product
    @PutMapping("/admin/products/{id}")
    @Operation(summary = "Update product", description = "Update existing product (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        System.out.println("📤 Admin updating product: " + id);
        System.out.println("   New name: " + product.getName());
        System.out.println("   New price: " + product.getPrice());
        
        Product updated = productService.updateProduct(id, product);
        System.out.println("✅ Product updated");
        return ResponseEntity.ok(updated);
    }
    
    // ✅ Admin - Delete product
    @DeleteMapping("/admin/products/{id}")
    @Operation(summary = "Delete product", description = "Delete product (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        System.out.println("🗑️ Admin deleting product: " + id);
        productService.deleteProduct(id);
        System.out.println("✅ Product deleted");
        return ResponseEntity.ok().build();
    }
    
    // ✅ Admin - Upload product image
    @PostMapping("/admin/products/{id}/images")
    @Operation(summary = "Upload product image", description = "Add image to product (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> uploadProductImage(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile file) {
        System.out.println("📤 Admin uploading image for product: " + id);
        System.out.println("   Filename: " + file.getOriginalFilename());
        System.out.println("   Size: " + file.getSize() + " bytes");
        
        productService.uploadProductImage(id, file);
        System.out.println("✅ Image uploaded successfully");
        return ResponseEntity.ok().build();
    }
    
    // ✅ Admin - Delete product image
    @DeleteMapping("/admin/products/images/{imageId}")
    @Operation(summary = "Delete product image", description = "Remove image from product (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> deleteProductImage(@PathVariable Long imageId) {
        System.out.println("🗑️ Admin deleting product image: " + imageId);
        productService.deleteProductImage(imageId);
        System.out.println("✅ Product image deleted");
        return ResponseEntity.ok().build();
    }
    
    // ✅ Admin - Get product stats
    @GetMapping("/admin/products/stats")
    @Operation(summary = "Get product statistics", description = "Get total product count (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> getProductStats() {
        System.out.println("📥 Admin fetching product stats");
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProducts", productService.getTotalProducts());
        System.out.println("✅ Total products: " + stats.get("totalProducts"));
        return ResponseEntity.ok(stats);
    }
}
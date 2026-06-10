
package com.example.Bananashop.service;

import com.example.Bananashop.model.Product;
import com.example.Bananashop.model.ProductImage;
import com.example.Bananashop.repository.ProductRepository;
import com.example.Bananashop.repository.ProductImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ProductImageRepository productImageRepository;
    
    @Value("${upload.path}")
    private String uploadPath;
    
    public List<Product> getAllProducts(String category) {
        if (category != null && !category.equals("ALL")) {
            Product.Category cat = Product.Category.valueOf(category);
            return productRepository.findByCategory(cat);
        }
        return productRepository.findAll();
    }
    
    public Product getProductById(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));
    }
    
    @Transactional
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }
    
    @Transactional
    public Product updateProduct(Long id, Product productDetails) {
        Product product = getProductById(id);
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setCategory(productDetails.getCategory());
        product.setStock(productDetails.getStock());
        return productRepository.save(product);
    }
    
    @Transactional
    public void uploadProductImage(Long productId, MultipartFile file) {
        try {
            Product product = getProductById(productId);
            
            // Create upload directory if not exists
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            
            // Generate unique filename
            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath);
            
            // Save to database
            ProductImage image = new ProductImage();
            image.setProduct(product);
            image.setImageUrl("/uploads/" + filename);
            
            productImageRepository.save(image);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image", e);
        }
    }
    
    @Transactional
    public void deleteProductImage(Long imageId) {
        productImageRepository.deleteById(imageId);
    }
    
    @Transactional
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
    
    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }
    
    public long getTotalProducts() {
        return productRepository.count();
    }
}
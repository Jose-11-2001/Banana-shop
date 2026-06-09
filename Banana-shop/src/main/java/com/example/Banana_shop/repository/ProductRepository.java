// repository/ProductRepository.java - Add missing method
package com.bananashop.repository;

import com.bananashop.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Find products by category
    List<Product> findByCategory(Product.Category category);
    
    // Search products by name (case insensitive)
    List<Product> findByNameContainingIgnoreCase(String name);
    
    // Add this method if missing
    boolean existsById(Long id);
}
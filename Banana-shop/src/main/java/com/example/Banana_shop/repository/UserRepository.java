// repository/UserRepository.java - Add missing methods
package com.bananashop.repository;

import com.bananashop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    // Add this method if missing
    List<User> findByRole(User.Role role);
}
package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * User Repository - Data access layer for User entity
 * 
 * PURPOSE: Provides database operations for User authentication and authorization
 * 
 * WHY: Repository pattern separates data access logic from business logic
 * HOW: Spring Data JPA auto-implements CRUD operations and custom queries
 * WHERE USED: By UserDetailsService for authentication and user management services
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * AUTHENTICATION: Find user by username for login
     * 
     * WHY: During authentication, we need to load user details by their username
     * HOW: Spring Security's UserDetailsService calls this method
     * RETURNS: Optional to handle cases where username doesn't exist
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Check if username already exists (for registration validation)
     * 
     * WHY: Prevents duplicate usernames during user registration
     * WHERE USED: In registration service before creating new user
     */
    boolean existsByUsername(String username);
}

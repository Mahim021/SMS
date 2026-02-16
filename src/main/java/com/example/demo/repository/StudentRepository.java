package com.example.demo.repository;

import com.example.demo.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Student Repository - Data access layer for Student entity
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    
    /**
     * Find student by email
     */
    Optional<Student> findByEmail(String email);
    
    /**
     * Check if email already exists
     */
    boolean existsByEmail(String email);
}

package com.example.demo.repository;

import com.example.demo.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Teacher Repository - Data access layer for Teacher entity
 */
@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    
    /**
     * Find teacher by email
     */
    Optional<Teacher> findByEmail(String email);
    
    /**
     * Check if email already exists
     */
    boolean existsByEmail(String email);
}

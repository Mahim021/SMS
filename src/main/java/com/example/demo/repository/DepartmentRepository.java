package com.example.demo.repository;

import com.example.demo.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Department Repository - Data access layer for Department entity
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    
    /**
     * Find department by name
     */
    Optional<Department> findByName(String name);
    
    /**
     * Check if department name already exists
     */
    boolean existsByName(String name);
}

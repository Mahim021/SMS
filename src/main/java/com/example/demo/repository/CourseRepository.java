package com.example.demo.repository;

import com.example.demo.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Course Repository - Data access layer for Course entity
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    /**
     * Find all courses by department ID
     */
    List<Course> findByDepartmentId(Long departmentId);
    
    /**
     * Find all courses taught by a specific teacher
     */
    List<Course> findByTeacherId(Long teacherId);
}

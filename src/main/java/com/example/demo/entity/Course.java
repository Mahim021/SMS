package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Course Entity - Represents courses offered in the system
 * Courses are associated with departments, taught by teachers, and enrolled by students
 */
@Entity
@Table(name = "courses")
@Data  // Lombok: Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor  // Lombok: Required by JPA for entity instantiation
@AllArgsConstructor  // Lombok: Creates constructor with all fields
public class Course {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(length = 1000)
    private String description;
    
    // Many courses belong to one department
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;
    
    // Many courses are taught by one teacher
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;
    
    // Many-to-many: Courses have multiple students enrolled
    @ManyToMany(mappedBy = "courses")
    private List<Student> students = new ArrayList<>();
}

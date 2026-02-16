package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Department Entity - Represents academic departments in the system
 * Contains bidirectional relationships with Student, Teacher, and Course entities
 */
@Entity
@Table(name = "departments")
@Data  // Lombok: Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor  // Lombok: Required by JPA for entity instantiation
@AllArgsConstructor  // Lombok: Creates constructor with all fields
public class Department {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    // One department has many students
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Student> students = new ArrayList<>();
    
    // One department has many teachers
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Teacher> teachers = new ArrayList<>();
    
    // One department offers many courses
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Course> courses = new ArrayList<>();
}

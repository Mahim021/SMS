package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Teacher Entity - Represents teachers in the system
 * Teachers have TEACHER role with elevated permissions (can modify student information)
 * Linked to User entity for authentication purposes
 */
@Entity
@Table(name = "teachers")
@Data  // Lombok: Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor  // Lombok: Required by JPA for entity instantiation
@AllArgsConstructor  // Lombok: Creates constructor with all fields
public class Teacher {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(unique = true)
    private String email;
    
    // Many teachers belong to one department
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;
    
    // One teacher teaches multiple courses
    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Course> courses = new ArrayList<>();
    
    // One-to-one relationship with User for authentication
    // This links the teacher entity to login credentials and role-based access
    @OneToOne(mappedBy = "teacher", cascade = CascadeType.ALL)
    private User user;
}

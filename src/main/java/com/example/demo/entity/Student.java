package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Student Entity - Represents students in the system
 * Students have USER role and limited permissions (cannot modify teacher profiles)
 * Linked to User entity for authentication purposes
 */
@Entity
@Table(name = "students")
@Data  // Lombok: Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor  // Lombok: Required by JPA for entity instantiation
@AllArgsConstructor  // Lombok: Creates constructor with all fields
public class Student {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    // Many students belong to one department
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;
    
    // Many-to-many: Students can enroll in multiple courses
    @ManyToMany
    @JoinTable(
            name = "student_courses",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")  // Fixed: was singular
    )
    private List<Course> courses = new ArrayList<>();
    
    // One-to-one relationship with User for authentication
    // This links the student entity to login credentials and role-based access
    @OneToOne(mappedBy = "student", cascade = CascadeType.ALL)
    private User user;
}

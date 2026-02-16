package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User Entity - Central authentication and authorization entity
 * 
 * PURPOSE: This entity manages user authentication and role-based access control (RBAC)
 * 
 * WHY: Implements security requirements where:
 *  - Students (ROLE_STUDENT) have limited access - cannot modify teacher profiles
 *  - Teachers (ROLE_TEACHER) have elevated access - can modify student information
 * 
 * HOW: Uses Spring Security's UserDetails pattern for authentication
 *  - Username and password are used for login credentials
 *  - Role field determines authorization level (STUDENT or TEACHER)
 *  - One-to-One relationship links User to either Student or Teacher entity
 */
@Entity
@Table(name = "users")
@Data  // Lombok: Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor  // Lombok: Required by JPA for entity instantiation
@AllArgsConstructor  // Lombok: Creates constructor with all fields
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * AUTHENTICATION: Username is used as the principal identifier during login
     * Must be unique to prevent authentication conflicts
     */
    @Column(nullable = false, unique = true)
    private String username;
    
    /**
     * AUTHENTICATION: Password should be stored as BCrypt hash (handled by SecurityConfig)
     * Never store plain text passwords in the database
     */
    @Column(nullable = false)
    private String password;
    
    /**
     * AUTHORIZATION: Role determines what actions the user can perform
     * - ROLE_STUDENT: Can view their own data, cannot modify teacher profiles
     * - ROLE_TEACHER: Can view and modify student data, view teacher profiles
     * 
     * Spring Security uses this for @PreAuthorize and hasRole() checks
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    
    /**
     * Account status flags for authentication
     * These are used by Spring Security's UserDetails interface
     */
    @Column(nullable = false)
    private Boolean enabled = true;  // Can the user login?
    
    @Column(nullable = false)
    private Boolean accountNonLocked = true;  // Is the account locked?
    
    /**
     * One-to-One relationship with Student entity
     * If this user is a student, this field links to their student profile
     * NULL if the user is a teacher
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", unique = true)
    private Student student;
    
    /**
     * One-to-One relationship with Teacher entity
     * If this user is a teacher, this field links to their teacher profile
     * NULL if the user is a student
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", unique = true)
    private Teacher teacher;
    
    /**
     * Role Enum - Defines the two user roles in the system
     * 
     * AUTHORIZATION: These roles are checked throughout the application using:
     * - @PreAuthorize("hasRole('TEACHER')") on controller methods
     * - SecurityFilterChain rules in SecurityConfig
     * - hasAuthority() checks in services
     */
    public enum Role {
        /**
         * ROLE_STUDENT: Limited privileges
         * - Can view their own student profile
         * - Can view courses they're enrolled in
         * - Cannot modify teacher information
         * - Cannot modify other students' information
         */
        ROLE_STUDENT,
        
        /**
         * ROLE_TEACHER: Elevated privileges
         * - Can view and modify student profiles
         * - Can view their own teacher profile
         * - Can manage courses they teach
         * - Can view other teachers (but not modify without additional permission)
         */
        ROLE_TEACHER
    }
}

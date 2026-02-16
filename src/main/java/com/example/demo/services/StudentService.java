package com.example.demo.services;

import com.example.demo.entity.Student;
import com.example.demo.entity.User;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Student Service - Business logic for student management
 * 
 * PURPOSE: Handles student CRUD operations with role-based authorization
 * 
 * AUTHORIZATION RULES IMPLEMENTED:
 * 1. Students can only view their own profile
 * 2. Teachers can view and modify all student profiles
 * 3. Students CANNOT modify teacher profiles (enforced in separate service)
 */
@Service
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentService(StudentRepository studentRepository, 
                         UserRepository userRepository,
                         PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * GET ALL STUDENTS - Teachers only
     * 
     * AUTHORIZATION: @PreAuthorize ensures only users with ROLE_TEACHER can call this method
     * WHY: Students should not see other students' full information
     * HOW: Spring Security checks user's authorities before method execution
     * 
     * @throws AccessDeniedException if called by non-teacher
     */
    @PreAuthorize("hasRole('TEACHER')")
    public List<Student> getAllStudents() {
        // AUTHORIZATION CHECK: Only reachable if user has ROLE_TEACHER
        // Spring Security blocks execution for ROLE_STUDENT users
        return studentRepository.findAll();
    }

    /**
     * GET STUDENT BY ID - Role-based access
     * 
     * AUTHORIZATION LOGIC:
     * - Teachers can view any student
     * - Students can only view their own profile
     * 
     * WHY: Prevents students from accessing other students' private information
     * HOW: Checks current user's role and student association
     */
    public Student getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + id));

        // AUTHORIZATION CHECK: Verify user has permission to view this student
        checkStudentAccessPermission(student);
        
        return student;
    }

    /**
     * CREATE STUDENT - Teachers only
     * 
     * AUTHORIZATION: Only teachers can create new student accounts
     * WHY: Students should not be able to create arbitrary student accounts
     */
    @PreAuthorize("hasRole('TEACHER')")
    public Student createStudent(Student student, String username, String password) {
        // Save student entity first
        Student savedStudent = studentRepository.save(student);

        // Create associated user account with ROLE_STUDENT
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));  // BCrypt hash
        user.setRole(User.Role.ROLE_STUDENT);
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        user.setStudent(savedStudent);

        userRepository.save(user);
        savedStudent.setUser(user);

        return savedStudent;
    }

    /**
     * UPDATE STUDENT - Teachers only
     * 
     * AUTHORIZATION: @PreAuthorize ensures only ROLE_TEACHER can modify students
     * WHY: This implements the key requirement - teachers can modify student info
     * HOW: Method blocked for ROLE_STUDENT users
     * 
     * BUSINESS RULE: Teachers can update student profiles, students cannot
     */
    @PreAuthorize("hasRole('TEACHER')")
    public Student updateStudent(Long id, Student studentDetails) {
        // AUTHORIZATION CHECK: Only reachable if user has ROLE_TEACHER
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + id));

        // Update fields
        student.setName(studentDetails.getName());
        student.setEmail(studentDetails.getEmail());
        student.setDepartment(studentDetails.getDepartment());

        return studentRepository.save(student);
    }

    /**
     * DELETE STUDENT - Teachers only
     * 
     * AUTHORIZATION: Only teachers can delete student accounts
     */
    @PreAuthorize("hasRole('TEACHER')")
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + id));
        studentRepository.delete(student);
    }

    /**
     * HELPER METHOD: Check if current user can access this student's data
     * 
     * AUTHORIZATION LOGIC:
     * 1. If user is TEACHER - allow access to any student
     * 2. If user is STUDENT - only allow access to their own profile
     * 3. Otherwise - deny access
     * 
     * WHY: Implements fine-grained authorization beyond simple role checks
     * HOW: Compares authenticated user's student ID with requested student
     * 
     * @throws AccessDeniedException if user lacks permission
     */
    private void checkStudentAccessPermission(Student student) {
        // Get currently authenticated user from Spring Security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        // Load current user from database
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        // AUTHORIZATION CHECK 1: Teachers can access any student
        if (currentUser.getRole() == User.Role.ROLE_TEACHER) {
            return;  // Access granted
        }

        // AUTHORIZATION CHECK 2: Students can only access their own profile
        if (currentUser.getRole() == User.Role.ROLE_STUDENT) {
            if (currentUser.getStudent() == null || 
                !currentUser.getStudent().getId().equals(student.getId())) {
                // Student trying to access someone else's profile
                throw new AccessDeniedException(
                    "Students can only access their own profile");
            }
            return;  // Access granted to own profile
        }

        // AUTHORIZATION CHECK 3: Unknown role - deny access
        throw new AccessDeniedException("Access denied");
    }

    /**
     * GET CURRENT STUDENT - For student users to access their own profile
     * 
     * AUTHORIZATION: Any authenticated student can access their own profile
     * WHY: Students need to view their own information
     */
    @PreAuthorize("hasRole('STUDENT')")
    public Student getCurrentStudent() {
        // Get currently authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        if (currentUser.getStudent() == null) {
            throw new RuntimeException("No student profile associated with this user");
        }

        return currentUser.getStudent();
    }
}

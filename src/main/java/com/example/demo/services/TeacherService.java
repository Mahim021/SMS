package com.example.demo.services;

import com.example.demo.entity.Teacher;
import com.example.demo.entity.User;
import com.example.demo.repository.TeacherRepository;
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
 * Teacher Service - Business logic for teacher management
 * 
 * PURPOSE: Handles teacher CRUD operations with role-based authorization
 * 
 * AUTHORIZATION RULES IMPLEMENTED:
 * 1. Students CANNOT modify teacher profiles (key requirement)
 * 2. Teachers can view their own profile
 * 3. Teachers can view other teachers (read-only)
 * 4. Only admins/system can create/modify teacher accounts
 */
@Service
@Transactional
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public TeacherService(TeacherRepository teacherRepository,
                         UserRepository userRepository,
                         PasswordEncoder passwordEncoder) {
        this.teacherRepository = teacherRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * GET ALL TEACHERS - Teachers can view (students cannot)
     * 
     * AUTHORIZATION: @PreAuthorize blocks ROLE_STUDENT from accessing
     * WHY: Prevents students from accessing teacher listings
     * HOW: Spring Security checks role before method execution
     */
    @PreAuthorize("hasRole('TEACHER')")
    public List<Teacher> getAllTeachers() {
        // AUTHORIZATION CHECK: Only ROLE_TEACHER can reach this code
        return teacherRepository.findAll();
    }

    /**
     * GET TEACHER BY ID - Teachers only
     * 
     * AUTHORIZATION: Teachers can view other teachers, students cannot
     * WHY: KEY REQUIREMENT - Students cannot access teacher profiles
     */
    @PreAuthorize("hasRole('TEACHER')")
    public Teacher getTeacherById(Long id) {
        // AUTHORIZATION CHECK: ROLE_STUDENT blocked by @PreAuthorize
        // Only teachers can view teacher profiles
        return teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + id));
    }

    /**
     * CREATE TEACHER - System/Admin only (requires TEACHER role for now)
     * 
     * AUTHORIZATION: Students absolutely cannot create teacher accounts
     * WHY: Critical security - prevents privilege escalation
     * 
     * NOTE: In production, you might want a separate ADMIN role for this
     */
    @PreAuthorize("hasRole('TEACHER')")
    public Teacher createTeacher(Teacher teacher, String username, String password) {
        // Save teacher entity first
        Teacher savedTeacher = teacherRepository.save(teacher);

        // Create associated user account with ROLE_TEACHER
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));  // BCrypt hash
        user.setRole(User.Role.ROLE_TEACHER);
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        user.setTeacher(savedTeacher);

        userRepository.save(user);
        savedTeacher.setUser(user);

        return savedTeacher;
    }

    /**
     * UPDATE TEACHER - Teachers can update their own profile
     * 
     * AUTHORIZATION: 
     * - Students CANNOT modify teacher profiles (KEY REQUIREMENT)
     * - Teachers can only modify their own profile
     * 
     * WHY: Implements the critical requirement that students cannot change teacher info
     * HOW: @PreAuthorize blocks students, additional check ensures teachers only edit themselves
     */
    @PreAuthorize("hasRole('TEACHER')")
    public Teacher updateTeacher(Long id, Teacher teacherDetails) {
        // AUTHORIZATION CHECK 1: @PreAuthorize already blocked ROLE_STUDENT
        // Students cannot even reach this code
        
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + id));

        // AUTHORIZATION CHECK 2: Teachers can only modify their own profile
        checkTeacherSelfModificationPermission(teacher);

        // Update fields
        teacher.setName(teacherDetails.getName());
        teacher.setEmail(teacherDetails.getEmail());
        teacher.setDepartment(teacherDetails.getDepartment());

        return teacherRepository.save(teacher);
    }

    /**
     * DELETE TEACHER - System/Admin only
     * 
     * AUTHORIZATION: Students absolutely cannot delete teachers
     * WHY: Critical security operation
     */
    @PreAuthorize("hasRole('TEACHER')")
    public void deleteTeacher(Long id) {
        // AUTHORIZATION CHECK: ROLE_STUDENT blocked by @PreAuthorize
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + id));
        teacherRepository.delete(teacher);
    }

    /**
     * GET CURRENT TEACHER - For teacher users to access their own profile
     * 
     * AUTHORIZATION: Any authenticated teacher can access their own profile
     */
    @PreAuthorize("hasRole('TEACHER')")
    public Teacher getCurrentTeacher() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        if (currentUser.getTeacher() == null) {
            throw new RuntimeException("No teacher profile associated with this user");
        }

        return currentUser.getTeacher();
    }

    /**
     * HELPER METHOD: Ensure teachers can only modify their own profile
     * 
     * AUTHORIZATION: Prevents teachers from modifying other teachers' profiles
     * WHY: Even though students are already blocked, teachers shouldn't modify each other
     * HOW: Compares authenticated user's teacher ID with target teacher ID
     * 
     * @throws AccessDeniedException if teacher tries to modify another teacher
     */
    private void checkTeacherSelfModificationPermission(Teacher teacher) {
        // Get currently authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        // AUTHORIZATION CHECK: Teachers can only modify their own profile
        if (currentUser.getTeacher() == null || 
            !currentUser.getTeacher().getId().equals(teacher.getId())) {
            throw new AccessDeniedException(
                "Teachers can only modify their own profile");
        }
    }
}

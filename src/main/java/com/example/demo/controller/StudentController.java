package com.example.demo.controller;

import com.example.demo.entity.Student;
import com.example.demo.services.StudentService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Student Controller - Handles HTTP requests for student operations
 * 
 * PURPOSE: Web interface for student management with role-based access control
 * 
 * AUTHORIZATION STRATEGY:
 * - URL pattern /student/** requires ROLE_STUDENT (configured in SecurityConfig)
 * - URL pattern /teacher/students/** requires ROLE_TEACHER (configured in SecurityConfig)
 * - Service layer provides additional fine-grained authorization
 */
@Controller
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    /**
     * STUDENT DASHBOARD - Students view their own profile
     * 
     * AUTHORIZATION: 
     * - URL /student/dashboard requires ROLE_STUDENT (SecurityConfig)
     * - Students can only see their own information
     * 
     * WHY: Provides students access to their personal dashboard
     * HOW: Service layer ensures student only gets their own data
     */
    @GetMapping("/student/dashboard")
    public String studentDashboard(Model model) {
        try {
            // AUTHORIZATION: Service ensures student only gets their own profile
            Student currentStudent = studentService.getCurrentStudent();
            model.addAttribute("student", currentStudent);
            return "student/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * VIEW STUDENT PROFILE - Students view own, Teachers view any
     * 
     * AUTHORIZATION LOGIC:
     * - If accessed via /student/profile/{id} - must be student's own ID
     * - If accessed via /teacher/students/{id} - teacher can view any student
     * 
     * WHY: Students need to view their profile, teachers need to view student profiles
     * HOW: Service layer checks if user has permission to view this specific student
     */
    @GetMapping({"/student/profile/{id}", "/teacher/students/{id}"})
    public String viewStudentProfile(@PathVariable Long id, Model model) {
        try {
            // AUTHORIZATION: Service checks if current user can access this student
            Student student = studentService.getStudentById(id);
            model.addAttribute("student", student);
            return "student/profile";
        } catch (AccessDeniedException e) {
            model.addAttribute("error", "You don't have permission to view this student");
            return "error";
        }
    }

    /**
     * LIST ALL STUDENTS - Teachers only
     * 
     * AUTHORIZATION:
     * - URL /teacher/students requires ROLE_TEACHER (SecurityConfig)
     * - Service layer @PreAuthorize ensures only teachers can list all students
     * 
     * WHY: Teachers need to see all students for management purposes
     * HOW: Students cannot access this URL pattern at all
     */
    @GetMapping("/teacher/students")
    public String listStudents(Model model) {
        // AUTHORIZATION: URL protected by SecurityConfig, service has @PreAuthorize
        List<Student> students = studentService.getAllStudents();
        model.addAttribute("students", students);
        return "teacher/students-list";
    }

    /**
     * SHOW CREATE STUDENT FORM - Teachers only
     * 
     * AUTHORIZATION: URL /teacher/students/create requires ROLE_TEACHER
     * WHY: Only teachers can create new student accounts
     */
    @GetMapping("/teacher/students/create")
    public String showCreateStudentForm(Model model) {
        model.addAttribute("student", new Student());
        return "teacher/student-form";
    }

    /**
     * CREATE STUDENT - Teachers only
     * 
     * AUTHORIZATION:
     * - URL requires ROLE_TEACHER (SecurityConfig)
     * - Service @PreAuthorize blocks ROLE_STUDENT
     * 
     * WHY: KEY REQUIREMENT - Students cannot create student accounts
     * HOW: Multi-layer authorization (URL + Service method)
     */
    @PostMapping("/teacher/students/create")
    public String createStudent(@ModelAttribute Student student,
                                @RequestParam String username,
                                @RequestParam String password,
                                Model model) {
        try {
            // AUTHORIZATION: Service method has @PreAuthorize("hasRole('TEACHER')")
            studentService.createStudent(student, username, password);
            return "redirect:/teacher/students";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "teacher/student-form";
        }
    }

    /**
     * SHOW EDIT STUDENT FORM - Teachers only
     * 
     * AUTHORIZATION: Teachers can edit students, students cannot
     * WHY: KEY REQUIREMENT - Teachers can modify student info
     */
    @GetMapping("/teacher/students/edit/{id}")
    public String showEditStudentForm(@PathVariable Long id, Model model) {
        try {
            Student student = studentService.getStudentById(id);
            model.addAttribute("student", student);
            return "teacher/student-edit-form";
        } catch (AccessDeniedException e) {
            model.addAttribute("error", "Access denied");
            return "error";
        }
    }

    /**
     * UPDATE STUDENT - Teachers only
     * 
     * AUTHORIZATION:
     * - URL /teacher/students/update/{id} requires ROLE_TEACHER
     * - Service @PreAuthorize ensures only teachers can modify
     * 
     * WHY: KEY REQUIREMENT - Teachers can modify student info, students cannot
     * HOW: @PreAuthorize("hasRole('TEACHER')") on service method blocks students
     * 
     * CRITICAL SECURITY: Students are completely blocked from modifying student records
     */
    @PostMapping("/teacher/students/update/{id}")
    public String updateStudent(@PathVariable Long id,
                               @ModelAttribute Student studentDetails,
                               Model model) {
        try {
            // AUTHORIZATION: Service has @PreAuthorize - ROLE_STUDENT cannot execute this
            studentService.updateStudent(id, studentDetails);
            return "redirect:/teacher/students";
        } catch (AccessDeniedException e) {
            model.addAttribute("error", "Only teachers can modify student information");
            return "error";
        }
    }

    /**
     * DELETE STUDENT - Teachers only
     * 
     * AUTHORIZATION: Only teachers can delete student accounts
     * WHY: Prevents students from deleting accounts
     */
    @PostMapping("/teacher/students/delete/{id}")
    public String deleteStudent(@PathVariable Long id, Model model) {
        try {
            // AUTHORIZATION: Service has @PreAuthorize("hasRole('TEACHER')")
            studentService.deleteStudent(id);
            return "redirect:/teacher/students";
        } catch (AccessDeniedException e) {
            model.addAttribute("error", "Access denied");
            return "error";
        }
    }
}

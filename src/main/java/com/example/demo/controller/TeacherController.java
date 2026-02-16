package com.example.demo.controller;

import com.example.demo.entity.Teacher;
import com.example.demo.services.TeacherService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Teacher Controller - Handles HTTP requests for teacher operations
 * 
 * PURPOSE: Web interface for teacher management with strict authorization
 * 
 * AUTHORIZATION STRATEGY:
 * - URL pattern /teacher/** requires ROLE_TEACHER (configured in SecurityConfig)
 * - Service layer blocks students from ANY teacher modifications
 * - KEY REQUIREMENT: Students cannot modify or change teacher profiles
 */
@Controller
@RequestMapping("/teacher")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    /**
     * TEACHER DASHBOARD - Teachers view their own profile
     * 
     * AUTHORIZATION:
     * - URL /teacher/dashboard requires ROLE_TEACHER (SecurityConfig)
     * - ROLE_STUDENT completely blocked from accessing
     * 
     * WHY: Provides teachers access to their dashboard
     * HOW: SecurityConfig blocks /teacher/** for ROLE_STUDENT
     */
    @GetMapping("/dashboard")
    public String teacherDashboard(Model model) {
        try {
            // AUTHORIZATION: Only ROLE_TEACHER can reach this code
            Teacher currentTeacher = teacherService.getCurrentTeacher();
            model.addAttribute("teacher", currentTeacher);
            return "teacher/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * LIST ALL TEACHERS - Teachers only
     * 
     * AUTHORIZATION:
     * - URL /teacher/teachers requires ROLE_TEACHER
     * - STUDENTS CANNOT ACCESS - blocked by SecurityConfig
     * 
     * WHY: KEY REQUIREMENT - Students cannot view teacher listings
     * HOW: Multi-layer protection (URL + Service @PreAuthorize)
     */
    @GetMapping("/teachers")
    public String listTeachers(Model model) {
        // AUTHORIZATION: Service has @PreAuthorize("hasRole('TEACHER')")
        // Students cannot execute this even if they somehow reach this URL
        List<Teacher> teachers = teacherService.getAllTeachers();
        model.addAttribute("teachers", teachers);
        return "teacher/teachers-list";
    }

    /**
     * VIEW TEACHER PROFILE - Teachers only
     * 
     * AUTHORIZATION:
     * - URL requires ROLE_TEACHER
     * - STUDENTS COMPLETELY BLOCKED from viewing teacher profiles
     * 
     * WHY: KEY REQUIREMENT - Students cannot access teacher information
     * HOW: SecurityConfig blocks URL, Service has @PreAuthorize
     * 
     * CRITICAL SECURITY: This enforces the requirement that students cannot view teachers
     */
    @GetMapping("/profile/{id}")
    public String viewTeacherProfile(@PathVariable Long id, Model model) {
        try {
            // AUTHORIZATION: Service blocks ROLE_STUDENT with @PreAuthorize
            Teacher teacher = teacherService.getTeacherById(id);
            model.addAttribute("teacher", teacher);
            return "teacher/profile";
        } catch (AccessDeniedException e) {
            model.addAttribute("error", "Students cannot access teacher profiles");
            return "error";
        }
    }

    /**
     * SHOW EDIT TEACHER FORM - Teachers can edit their own profile
     * 
     * AUTHORIZATION:
     * - URL requires ROLE_TEACHER
     * - STUDENTS ABSOLUTELY CANNOT MODIFY TEACHERS (KEY REQUIREMENT)
     * 
     * WHY: Teachers need to update their own information
     * HOW: Service ensures teacher can only edit themselves, students blocked entirely
     */
    @GetMapping("/profile/edit/{id}")
    public String showEditTeacherForm(@PathVariable Long id, Model model) {
        try {
            // AUTHORIZATION: Service checks if teacher is editing themselves
            Teacher teacher = teacherService.getTeacherById(id);
            model.addAttribute("teacher", teacher);
            return "teacher/teacher-edit-form";
        } catch (AccessDeniedException e) {
            model.addAttribute("error", "Access denied");
            return "error";
        }
    }

    /**
     * UPDATE TEACHER PROFILE - Teachers can update their own profile
     * 
     * AUTHORIZATION:
     * - URL /teacher/profile/update/{id} requires ROLE_TEACHER
     * - Service ensures teacher only modifies their own profile
     * - STUDENTS ABSOLUTELY CANNOT EXECUTE THIS (KEY REQUIREMENT)
     * 
     * WHY: KEY REQUIREMENT - Students cannot modify or change teacher profiles
     * HOW: Multiple authorization layers:
     *   1. SecurityConfig blocks /teacher/** for ROLE_STUDENT
     *   2. Service @PreAuthorize("hasRole('TEACHER')") blocks ROLE_STUDENT
     *   3. Service checks teacher is modifying only themselves
     * 
     * CRITICAL SECURITY: This is the core enforcement of "students cannot change teacher profiles"
     */
    @PostMapping("/profile/update/{id}")
    public String updateTeacher(@PathVariable Long id,
                               @ModelAttribute Teacher teacherDetails,
                               Model model) {
        try {
            // AUTHORIZATION LAYER 1: URL blocked for ROLE_STUDENT (SecurityConfig)
            // AUTHORIZATION LAYER 2: Service method has @PreAuthorize("hasRole('TEACHER')")
            // AUTHORIZATION LAYER 3: Service checks teacher is editing themselves
            
            teacherService.updateTeacher(id, teacherDetails);
            return "redirect:/teacher/dashboard";
        } catch (AccessDeniedException e) {
            // This error appears if a student somehow bypasses URL security
            model.addAttribute("error", "Students cannot modify teacher profiles - Access Denied");
            return "error";
        }
    }

    /**
     * CREATE TEACHER - System/Admin function
     * 
     * AUTHORIZATION:
     * - Requires ROLE_TEACHER (for now)
     * - STUDENTS CANNOT CREATE TEACHER ACCOUNTS
     * 
     * WHY: Prevents privilege escalation
     * NOTE: In production, might want separate ADMIN role
     */
    @GetMapping("/teachers/create")
    public String showCreateTeacherForm(Model model) {
        model.addAttribute("teacher", new Teacher());
        return "teacher/teacher-form";
    }

    @PostMapping("/teachers/create")
    public String createTeacher(@ModelAttribute Teacher teacher,
                                @RequestParam String username,
                                @RequestParam String password,
                                Model model) {
        try {
            // AUTHORIZATION: Service has @PreAuthorize("hasRole('TEACHER')")
            teacherService.createTeacher(teacher, username, password);
            return "redirect:/teacher/teachers";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "teacher/teacher-form";
        }
    }

    /**
     * DELETE TEACHER - System/Admin function
     * 
     * AUTHORIZATION: Students absolutely cannot delete teachers
     */
    @PostMapping("/teachers/delete/{id}")
    public String deleteTeacher(@PathVariable Long id, Model model) {
        try {
            // AUTHORIZATION: Service has @PreAuthorize("hasRole('TEACHER')")
            teacherService.deleteTeacher(id);
            return "redirect:/teacher/teachers";
        } catch (AccessDeniedException e) {
            model.addAttribute("error", "Access denied");
            return "error";
        }
    }
}

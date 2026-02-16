package com.example.demo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Authentication Controller - Handles login, logout, and dashboard routing
 * 
 * PURPOSE: Manages user authentication flow and role-based dashboard redirection
 * 
 * AUTHENTICATION: How users prove their identity (login with username/password)
 * AUTHORIZATION: Where users are routed after login based on their role
 */
@Controller
public class AuthController {

    /**
     * HOME PAGE - Redirects to login if not authenticated
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }
    
    /**
     * LOGIN PAGE
     * 
     * AUTHENTICATION ENTRY POINT:
     * WHY: Users must authenticate before accessing protected resources
     * HOW: Spring Security processes login form and validates credentials
     * WHERE: SecurityConfig defines this as public URL
     */
    @GetMapping("/login")
    public String login() {
        // Spring Security automatically handles authentication when form is submitted
        // If authentication succeeds, user is redirected to /dashboard (see below)
        return "login";
    }

    /**
     * DASHBOARD - Role-based redirect after authentication
     * 
     * PURPOSE: Routes authenticated users to appropriate dashboard based on their role
     * 
     * AUTHORIZATION LOGIC:
     * - If user has ROLE_TEACHER → redirect to teacher dashboard
     * - If user has ROLE_STUDENT → redirect to student dashboard
     * - Otherwise → error page
     * 
     * WHY: Different roles have different interfaces and capabilities
     * HOW: Checks user's authorities (roles) from Spring Security context
     * WHERE: Called after successful login (configured in SecurityConfig)
     */
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        // AUTHORIZATION: Check user's role and route accordingly
        
        if (authentication == null || !authentication.isAuthenticated()) {
            // User not authenticated - send to login
            return "redirect:/login";
        }

        // Get user's authorities (roles) from authentication object
        // Spring Security populates this during authentication
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();
            
            // AUTHORIZATION: Route ROLE_TEACHER to teacher dashboard
            if (role.equals("ROLE_TEACHER")) {
                return "redirect:/teacher/dashboard";
            }
            
            // AUTHORIZATION: Route ROLE_STUDENT to student dashboard
            if (role.equals("ROLE_STUDENT")) {
                return "redirect:/student/dashboard";
            }
        }

        // Unknown role - should not happen in this system
        return "redirect:/login?error=true";
    }

    /**
     * ACCESS DENIED PAGE
     * 
     * AUTHORIZATION: Shown when user tries to access resource they don't have permission for
     * 
     * EXAMPLES:
     * - Student tries to access /teacher/dashboard
     * - Student tries to modify teacher profile
     * - Student tries to view all students
     * 
     * WHY: Provides user feedback when authorization fails
     * HOW: Spring Security redirects here when access is denied
     */
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/access-denied";
    }
}

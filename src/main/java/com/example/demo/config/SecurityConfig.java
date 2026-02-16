package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security Configuration - Central configuration for authentication and authorization
 * 
 * PURPOSE: Configures Spring Security for role-based access control
 * 
 * WHY EnableWebSecurity: Activates Spring Security's web security features
 * WHY EnableMethodSecurity: Allows @PreAuthorize annotations on methods for fine-grained access control
 * 
 * AUTHENTICATION: How users prove their identity (username + password)
 * AUTHORIZATION: What authenticated users are allowed to do based on their role
 */
@Configuration
@EnableWebSecurity  // Enables Spring Security's web security support
@EnableMethodSecurity(prePostEnabled = true)  // Enables @PreAuthorize, @PostAuthorize annotations
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * PASSWORD ENCODER BEAN
     * 
     * WHY: Passwords must be hashed before storing in database (security best practice)
     * HOW: BCrypt is a strong one-way hashing algorithm with built-in salt
     * WHERE USED: During user registration and login authentication
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // Uses BCrypt hashing with default strength (10 rounds)
    }

    /**
     * AUTHENTICATION PROVIDER
     * 
     * WHY: Defines how Spring Security authenticates users
     * HOW: 
     *  1. Uses UserDetailsService to load user by username from database
     *  2. Uses PasswordEncoder to verify submitted password against stored hash
     * WHERE USED: During login process when user submits credentials
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        // Spring Boot 4.x: DaoAuthenticationProvider constructor requires UserDetailsService
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());      // Verify passwords with BCrypt
        return authProvider;
    }

    /**
     * AUTHENTICATION MANAGER
     * 
     * WHY: Central interface for authentication in Spring Security
     * HOW: Delegates to authentication provider to verify credentials
     * WHERE USED: By login controllers and security filters
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * SECURITY FILTER CHAIN - URL-based Authorization Rules
     * 
     * PURPOSE: Defines which URLs require authentication and what roles can access them
     * 
     * AUTHORIZATION STRATEGY:
     * 1. Public endpoints: /login, /register, /css, /js - anyone can access
     * 2. Student endpoints: /student/** - requires ROLE_STUDENT
     * 3. Teacher endpoints: /teacher/** - requires ROLE_TEACHER
     * 4. All other endpoints: require authentication
     * 
     * WHY Form Login: Provides default login form and handles authentication
     * WHY Logout: Clears session and redirects to login page
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            /**
             * AUTHORIZATION RULES - URL-based access control
             * 
             * Order matters! More specific rules should come before general ones
             */
            .authorizeHttpRequests(authorize -> authorize
                // PUBLIC ACCESS - No authentication required
                // WHY: Users need to access login page and static resources before authenticating
                .requestMatchers("/", "/login", "/dashboard", "/register", "/css/**", "/js/**", "/images/**", "/error", "/h2-console/**").permitAll()
                
                // STUDENT ROLE REQUIRED - AUTHORIZATION
                // WHY: Students can only access their own dashboard and profile
                // HOW: Spring Security checks if authenticated user has ROLE_STUDENT
                .requestMatchers("/student/**").hasRole("STUDENT")
                
                // TEACHER ROLE REQUIRED - AUTHORIZATION
                // WHY: Teachers need elevated access to manage students
                // HOW: Spring Security checks if authenticated user has ROLE_TEACHER
                .requestMatchers("/teacher/**").hasRole("TEACHER")
                
                // ADMIN/MANAGEMENT ENDPOINTS - Require TEACHER role
                // WHY: Only teachers can manage courses and view all students
                .requestMatchers("/courses/manage/**", "/students/manage/**").hasRole("TEACHER")
                
                // ALL OTHER ENDPOINTS - Require authentication (any role)
                // WHY: Default security posture - deny unless explicitly permitted
                .anyRequest().authenticated()
            )
            
            /**
             * FORM LOGIN CONFIGURATION - AUTHENTICATION
             * 
             * WHY: Provides standard username/password login mechanism
             * HOW: 
             *  1. User submits credentials to /login
             *  2. Spring Security validates using UserDetailsService
             *  3. On success, redirects based on user's role
             */
            .formLogin(form -> form
                .loginPage("/login")              // Custom login page URL
                .loginProcessingUrl("/perform-login")     // Where form submits credentials (must be different)
                .defaultSuccessUrl("/dashboard", true)  // Redirect after successful login
                .failureUrl("/login?error=true")  // Redirect if authentication fails
                .permitAll()
            )
            
            // Disable CSRF for H2 console
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")
            )
            
            // Allow frames for H2 console
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            )
            
            /**
             * LOGOUT CONFIGURATION
             * 
             * WHY: Securely terminate user session
             * HOW: Invalidates session, clears authentication, deletes cookies
             */
            .logout(logout -> logout
                .logoutUrl("/logout")             // URL to trigger logout
                .logoutSuccessUrl("/login?logout=true")  // Redirect after logout
                .invalidateHttpSession(true)      // Destroy session
                .deleteCookies("JSESSIONID")      // Remove session cookie
                .permitAll()
            );

        return http.build();
    }
}

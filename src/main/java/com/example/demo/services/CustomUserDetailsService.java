package com.example.demo.services;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;

/**
 * Custom UserDetailsService Implementation - Core of Spring Security Authentication
 * 
 * PURPOSE: Loads user-specific data during authentication process
 * 
 * WHY UserDetailsService: Spring Security requires this interface to load user data
 * HOW IT WORKS:
 *  1. User submits username/password at login
 *  2. Spring Security calls loadUserByUsername(username)
 *  3. This service queries database for user
 *  4. Returns UserDetails object with user info and authorities
 *  5. Spring Security compares passwords and grants access if valid
 * 
 * AUTHENTICATION FLOW:
 *  Login Form → Spring Security → loadUserByUsername() → Database → UserDetails → Password Check → Session Created
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * CORE AUTHENTICATION METHOD
     * 
     * WHY @Override: Implements Spring Security's UserDetailsService contract
     * WHY @Transactional: Ensures database connection stays open for lazy-loaded relationships
     * 
     * AUTHENTICATION PROCESS:
     * 1. Spring Security calls this method with the username from login form
     * 2. Query database to find user by username
     * 3. If not found, throw exception (authentication fails)
     * 4. If found, convert User entity to UserDetails object
     * 5. Spring Security then validates the password
     * 
     * @param username - The username submitted in login form
     * @return UserDetails - Spring Security's representation of authenticated user
     * @throws UsernameNotFoundException - If user doesn't exist (authentication fails)
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        // AUTHENTICATION STEP 1: Find user in database
        // WHY orElseThrow: If user doesn't exist, authentication must fail
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username: " + username));

        // AUTHENTICATION STEP 2: Check if account is enabled
        // WHY: Disabled accounts should not be able to login
        if (!user.getEnabled()) {
            throw new UsernameNotFoundException("Account is disabled");
        }

        // AUTHENTICATION STEP 3: Check if account is locked
        // WHY: Locked accounts (e.g., after failed login attempts) should not authenticate
        if (!user.getAccountNonLocked()) {
            throw new UsernameNotFoundException("Account is locked");
        }

        // AUTHENTICATION STEP 4: Build and return UserDetails object
        // Spring Security uses this to complete authentication
        return buildUserDetails(user);
    }

    /**
     * BUILD USER DETAILS - Convert domain User to Spring Security UserDetails
     * 
     * WHY: Spring Security needs UserDetails format, not our custom User entity
     * 
     * UserDetails contains:
     *  - Username: Identifier for the user
     *  - Password: BCrypt hash (Spring Security will compare with submitted password)
     *  - Authorities: Roles/permissions (used for AUTHORIZATION after authentication)
     *  - Account flags: enabled, locked, expired, credentials expired
     * 
     * AUTHORIZATION: The authorities/roles returned here are used throughout the app
     * for @PreAuthorize, hasRole(), and other authorization checks
     */
    private UserDetails buildUserDetails(User user) {
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                
                // PASSWORD: Already BCrypt hashed in database
                // Spring Security will use PasswordEncoder to verify it matches submitted password
                .password(user.getPassword())
                
                // AUTHORIZATION: Convert user's role to Spring Security authority
                // This is used for hasRole() checks throughout the application
                .authorities(getAuthorities(user))
                
                // ACCOUNT STATUS FLAGS for authentication
                .accountExpired(false)
                .accountLocked(!user.getAccountNonLocked())
                .credentialsExpired(false)
                .disabled(!user.getEnabled())
                
                .build();
    }

    /**
     * GET AUTHORITIES - Convert user role to Spring Security authorities
     * 
     * PURPOSE: Map our User.Role enum to Spring Security's GrantedAuthority
     * 
     * WHY GrantedAuthority: Spring Security's way of representing permissions
     * HOW: SimpleGrantedAuthority wraps role string (e.g., "ROLE_STUDENT")
     * 
     * AUTHORIZATION: These authorities are checked by:
     *  - @PreAuthorize("hasRole('TEACHER')") annotations
     *  - hasRole('STUDENT') in SecurityConfig
     *  - hasAuthority('ROLE_TEACHER') in service methods
     * 
     * IMPORTANT: Spring Security automatically adds "ROLE_" prefix when using hasRole()
     * So hasRole('STUDENT') checks for authority 'ROLE_STUDENT'
     */
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        // Convert user's role to GrantedAuthority
        // Example: User.Role.ROLE_STUDENT becomes SimpleGrantedAuthority("ROLE_STUDENT")
        return Collections.singleton(new SimpleGrantedAuthority(user.getRole().name()));
    }
}

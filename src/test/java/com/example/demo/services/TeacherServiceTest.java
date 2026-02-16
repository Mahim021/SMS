package com.example.demo.services;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.entity.Teacher;
import com.example.demo.entity.User;
import com.example.demo.repository.TeacherRepository;
import com.example.demo.repository.UserRepository;

/**
 * Simplified Unit Tests for TeacherService
 * 
 * PURPOSE: Test critical security - Students CANNOT access teacher data (3 key tests)
 * 
 * TESTS:
 * 1. Teachers can view other teachers
 * 2. Teacher can only update OWN profile (not other teachers)
 * 3. Create teacher works correctly
 * 
 * BENEFITS:
 * - Enforces KEY REQUIREMENT: student cannot access teacher information
 * - Tests critical security controls
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TeacherService Tests")
class TeacherServiceTest {

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TeacherService teacherService;

    private Teacher testTeacher;
    private Teacher anotherTeacher;
    private User testTeacherUser;
    private User anotherTeacherUser;

    @BeforeEach
    void setUp() {
        // Setup first teacher
        testTeacher = new Teacher();
        testTeacher.setId(1L);
        testTeacher.setName("Dr. Smith");
        testTeacher.setEmail("smith@teacher.com");

        // Setup teacher user
        testTeacherUser = new User();
        testTeacherUser.setId(1L);
        testTeacherUser.setUsername("teacher_smith");
        testTeacherUser.setPassword("encoded_password");
        testTeacherUser.setRole(User.Role.ROLE_TEACHER);
        testTeacherUser.setTeacher(testTeacher);

        testTeacher.setUser(testTeacherUser);

        // Setup another teacher
        anotherTeacher = new Teacher();
        anotherTeacher.setId(2L);
        anotherTeacher.setName("Dr. Johnson");
        anotherTeacher.setEmail("johnson@teacher.com");

        anotherTeacherUser = new User();
        anotherTeacherUser.setId(2L);
        anotherTeacherUser.setUsername("teacher_johnson");
        anotherTeacherUser.setPassword("encoded_password");
        anotherTeacherUser.setRole(User.Role.ROLE_TEACHER);
        anotherTeacherUser.setTeacher(anotherTeacher);

        anotherTeacher.setUser(anotherTeacherUser);

        // Setup SecurityContext mock
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("1. Teacher can view other teachers")
    void testGetAllTeachers_AsTeacher_Success() {
        // Arrange
        List<Teacher> teachers = Arrays.asList(testTeacher, anotherTeacher);
        when(teacherRepository.findAll()).thenReturn(teachers);

        // Act
        List<Teacher> result = teacherService.getAllTeachers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Dr. Smith", result.get(0).getName());
        assertEquals("Dr. Johnson", result.get(1).getName());
        verify(teacherRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("2. Teacher CANNOT update another teacher's profile - SECURITY TEST")
    void testUpdateTeacher_OtherTeacherProfile_ThrowsException() {
        // Arrange - Teacher Smith trying to update Teacher Johnson's profile
        Teacher updatedDetails = new Teacher();
        updatedDetails.setName("Malicious Update");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("teacher_smith");
        when(userRepository.findByUsername("teacher_smith")).thenReturn(Optional.of(testTeacherUser));
        when(teacherRepository.findById(2L)).thenReturn(Optional.of(anotherTeacher));

        // Act & Assert
        AccessDeniedException exception = assertThrows(
            AccessDeniedException.class,
            () -> teacherService.updateTeacher(2L, updatedDetails),
            "Teacher should NOT be able to modify another teacher's profile"
        );

        assertTrue(exception.getMessage().contains("Teachers can only modify their own profile"));
        verify(teacherRepository, never()).save(any());
    }

    @Test
    @DisplayName("3. Create teacher with valid data")
    void testCreateTeacher_ValidData_Success() {
        // Arrange
        Teacher newTeacher = new Teacher();
        newTeacher.setName("Dr. New Teacher");
        newTeacher.setEmail("new@teacher.com");

        when(teacherRepository.save(any(Teacher.class))).thenReturn(testTeacher);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(testTeacherUser);

        // Act
        Teacher result = teacherService.createTeacher(newTeacher, "new_teacher", "password123");

        // Assert
        assertNotNull(result);
        verify(teacherRepository, times(1)).save(any(Teacher.class));
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode("password123");
    }
}

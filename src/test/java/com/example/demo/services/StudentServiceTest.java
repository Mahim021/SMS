package com.example.demo.services;

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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.entity.Student;
import com.example.demo.entity.User;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.UserRepository;

/**
 * Simplified Unit Tests for StudentService
 * 
 * PURPOSE: Test only critical security and core functionality (3 key tests)
 * 
 * TESTS:
 * 1. Teacher can view any student (authorization)
 * 2. Student CANNOT view other student's profile (security)
 * 3. Create student works correctly (core functionality)
 * 
 * BENEFITS:
 * - Fast testing of critical security requirements
 * - Minimal test coverage for core operations
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StudentService Tests")
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private StudentService studentService;

    private Student testStudent;
    private User testStudentUser;
    private User testTeacherUser;

    @BeforeEach
    void setUp() {
        // Setup test student
        testStudent = new Student();
        testStudent.setId(1L);
        testStudent.setName("John Doe");
        testStudent.setEmail("john@student.com");

        // Setup student user
        testStudentUser = new User();
        testStudentUser.setId(1L);
        testStudentUser.setUsername("john_student");
        testStudentUser.setPassword("encoded_password");
        testStudentUser.setRole(User.Role.ROLE_STUDENT);
        testStudentUser.setStudent(testStudent);

        // Setup teacher user
        testTeacherUser = new User();
        testTeacherUser.setId(2L);
        testTeacherUser.setUsername("teacher1");
        testTeacherUser.setPassword("encoded_password");
        testTeacherUser.setRole(User.Role.ROLE_TEACHER);

        testStudent.setUser(testStudentUser);

        // Setup SecurityContext mock
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("1. Teacher can view any student profile")
    void testGetStudentById_AsTeacher_Success() {
        // Arrange - Teacher accessing student profile
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("teacher1");
        when(userRepository.findByUsername("teacher1")).thenReturn(Optional.of(testTeacherUser));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));

        // Act
        Student result = studentService.getStudentById(1L);

        // Assert
        assertNotNull(result, "Student should be found");
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        verify(studentRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("2. Student CANNOT view other student's profile - SECURITY TEST")
    void testGetStudentById_StudentAccessingOtherProfile_ThrowsException() {
        // Arrange - Student trying to access another student's profile
        Student otherStudent = new Student();
        otherStudent.setId(99L);
        otherStudent.setName("Other Student");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("john_student");
        when(userRepository.findByUsername("john_student")).thenReturn(Optional.of(testStudentUser));
        when(studentRepository.findById(99L)).thenReturn(Optional.of(otherStudent));

        // Act & Assert
        AccessDeniedException exception = assertThrows(
            AccessDeniedException.class,
            () -> studentService.getStudentById(99L),
            "Should throw AccessDeniedException when student tries to access other's profile"
        );

        assertTrue(exception.getMessage().contains("Students can only access their own profile"));
    }

    @Test
    @DisplayName("3. Create student with valid data")
    void testCreateStudent_ValidData_Success() {
        // Arrange
        Student newStudent = new Student();
        newStudent.setName("New Student");
        newStudent.setEmail("new@student.com");

        when(studentRepository.save(any(Student.class))).thenReturn(testStudent);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(testStudentUser);

        // Act
        Student result = studentService.createStudent(newStudent, "new_username", "password123");

        // Assert
        assertNotNull(result, "Created student should not be null");
        verify(studentRepository, times(1)).save(any(Student.class));
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode("password123");
    }

    @Test
    @DisplayName("4. Find student by invalid ID throws exception")
    void testGetStudentById_InvalidId_ThrowsException() {
        // Arrange
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> studentService.getStudentById(999L),
            "Should throw exception when student not found"
        );

        assertTrue(exception.getMessage().contains("Student not found"));
    }
}

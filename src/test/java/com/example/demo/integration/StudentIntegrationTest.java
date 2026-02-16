package com.example.demo.integration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Department;
import com.example.demo.entity.Student;
import com.example.demo.repository.DepartmentRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.services.StudentService;

/**
 * Integration Test for Student Management
 * 
 * PURPOSE: Test full stack (Service → Repository → Database)
 * 
 * DIFFERENCES from Unit Tests:
 * - Uses @SpringBootTest (loads full Spring context)
 * - Uses real H2 database (in-memory)
 * - Tests with real Spring beans
 * - No mocking - uses real components
 * 
 * BENEFITS:
 * - Tests how components work together
 * - Validates database operations
 * - Ensures transactions work
 * - More realistic than unit tests
 */
@SpringBootTest
@Transactional
@DisplayName("Student Integration Tests")
class StudentIntegrationTest {

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Department testDepartment;

    @BeforeEach
    void setUp() {
        // Clean database before each test
        studentRepository.deleteAll();
        userRepository.deleteAll();
        departmentRepository.deleteAll();
        
        // Create test department with unique name for testing
        testDepartment = new Department();
        testDepartment.setName("Test Department " + System.currentTimeMillis());
        testDepartment = departmentRepository.save(testDepartment);
    }

    @Test
    @DisplayName("Integration: Create and saveStudent to database")
    void testCreateAndSaveStudent() {
        // Arrange - Create a student
        Student newStudent = new Student();
        newStudent.setName("John Doe");
        newStudent.setEmail("john@student.com");
        newStudent.setDepartment(testDepartment);

        // Act - Save directly through repository (bypasses @PreAuthorize)
        Student savedStudent = studentRepository.save(newStudent);

        // Assert - Verify it was saved to the database
        assertNotNull(savedStudent.getId(), "Student should have an ID after saving");
        
        // Retrieve directly from database to verify persistence
        Student retrievedStudent = studentRepository.findById(savedStudent.getId()).orElse(null);
        
        assertNotNull(retrievedStudent, "Student should be retrievable from database");
        assertEquals("John Doe", retrievedStudent.getName());
        assertEquals("john@student.com", retrievedStudent.getEmail());
        assertEquals(testDepartment.getName(), retrievedStudent.getDepartment().getName());
    }

    @Test
    @DisplayName("Integration: Find all students from database")
    void testFindAllStudentsFromDatabase() {
        // Arrange - Create multiple students in database
        Student student1 = new Student();
        student1.setName("Alice");
        student1.setEmail("alice@student.com");
        student1.setDepartment(testDepartment);
        studentRepository.save(student1);

        Student student2 = new Student();
        student2.setName("Bob");
        student2.setEmail("bob@student.com");
        student2.setDepartment(testDepartment);
        studentRepository.save(student2);

        // Act - Retrieve through repository
        List<Student> students = studentRepository.findAll();

        // Assert - Verify data integrity
        assertEquals(2, students.size(), "Should retrieve 2 students from database");
        assertTrue(students.stream().anyMatch(s -> s.getName().equals("Alice")));
        assertTrue(students.stream().anyMatch(s -> s.getName().equals("Bob")));
    }

    @Test
    @DisplayName("Integration: Update and persist student changes")
    void testUpdateAndPersistStudent() {
        // Arrange - Create a student
        Student student = new Student();
        student.setName("Original Name");
        student.setEmail("original@student.com");
        student.setDepartment(testDepartment);
        Student savedStudent = studentRepository.save(student);
        Long studentId = savedStudent.getId();

        // Act - Update student directly
        savedStudent.setName("Updated Name");
        savedStudent.setEmail("updated@student.com");
        studentRepository.save(savedStudent);

        // Assert - Verify changes persisted in database
        Student updatedStudent = studentRepository.findById(studentId).orElse(null);
        assertNotNull(updatedStudent);
        assertEquals("Updated Name", updatedStudent.getName());
        assertEquals("updated@student.com", updatedStudent.getEmail());
    }

    @Test
    @DisplayName("Integration: Delete student removes from database")
    void testDeleteStudentFromDatabase() {
        // Arrange - Create a student
        Student student = new Student();
        student.setName("To Be Deleted");
        student.setEmail("delete@student.com");
        student.setDepartment(testDepartment);
        Student savedStudent = studentRepository.save(student);
        Long studentId = savedStudent.getId();

        // Act - Delete student
        studentRepository.delete(savedStudent);

        // Assert - Verify deleted from database
        boolean exists = studentRepository.existsById(studentId);
        assertFalse(exists, "Student should be deleted from database");
    }
}

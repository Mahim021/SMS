package com.example.demo.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.entity.Course;
import com.example.demo.entity.Department;
import com.example.demo.entity.Student;
import com.example.demo.entity.Teacher;
import com.example.demo.entity.User;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.DepartmentRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.TeacherRepository;
import com.example.demo.repository.UserRepository;

/**
 * Data Initializer - Creates sample data on application startup
 * 
 * PURPOSE: Provides initial users and data for testing the authentication system
 * 
 * SAMPLE CREDENTIALS:
 * 
 * STUDENTS:
 *   Username: student1, Password: password123 (ROLE_STUDENT)
 *   Username: student2, Password: password123 (ROLE_STUDENT)
 * 
 * TEACHERS:
 *   Username: teacher1, Password: password123 (ROLE_TEACHER)
 *   Username: teacher2, Password: password123 (ROLE_TEACHER)
 */
@Configuration
public class DataInitializer {

    /**
     * Creates sample data on application startup
     * Only runs once when database is empty
     */
    @Bean
    public CommandLineRunner initData(
            UserRepository userRepository,
            StudentRepository studentRepository,
            TeacherRepository teacherRepository,
            DepartmentRepository departmentRepository,
            CourseRepository courseRepository,
            PasswordEncoder passwordEncoder) {
        
        return args -> {
            // Only initialize if no users exist
            if (userRepository.count() > 0) {
                System.out.println("Database already initialized. Skipping data creation.");
                return;
            }

            System.out.println("Initializing database with sample data...");

            // Create Departments
            Department csDept = new Department();
            csDept.setName("Computer Science");
            csDept = departmentRepository.save(csDept);

            Department mathDept = new Department();
            mathDept.setName("Mathematics");
            mathDept = departmentRepository.save(mathDept);

            // Create Students
            Student student1 = new Student();
            student1.setName("John Doe");
            student1.setEmail("john.doe@student.edu");
            student1.setDepartment(csDept);
            student1 = studentRepository.save(student1);

            Student student2 = new Student();
            student2.setName("Jane Smith");
            student2.setEmail("jane.smith@student.edu");
            student2.setDepartment(csDept);
            student2 = studentRepository.save(student2);

            // Create Teachers
            Teacher teacher1 = new Teacher();
            teacher1.setName("Dr. Alice Brown");
            teacher1.setEmail("alice.brown@teacher.edu");
            teacher1.setDepartment(csDept);
            teacher1 = teacherRepository.save(teacher1);

            Teacher teacher2 = new Teacher();
            teacher2.setName("Prof. Charlie Wilson");
            teacher2.setEmail("charlie.wilson@teacher.edu");
            teacher2.setDepartment(mathDept);
            teacher2 = teacherRepository.save(teacher2);

            // Create User accounts for students
            // Password: password123 (BCrypt encoded)
            User studentUser1 = new User();
            studentUser1.setUsername("student1");
            studentUser1.setPassword(passwordEncoder.encode("password123"));
            studentUser1.setRole(User.Role.ROLE_STUDENT);
            studentUser1.setEnabled(true);
            studentUser1.setAccountNonLocked(true);
            studentUser1.setStudent(student1);
            userRepository.save(studentUser1);

            User studentUser2 = new User();
            studentUser2.setUsername("student2");
            studentUser2.setPassword(passwordEncoder.encode("password123"));
            studentUser2.setRole(User.Role.ROLE_STUDENT);
            studentUser2.setEnabled(true);
            studentUser2.setAccountNonLocked(true);
            studentUser2.setStudent(student2);
            userRepository.save(studentUser2);

            // Create User accounts for teachers
            User teacherUser1 = new User();
            teacherUser1.setUsername("teacher1");
            teacherUser1.setPassword(passwordEncoder.encode("password123"));
            teacherUser1.setRole(User.Role.ROLE_TEACHER);
            teacherUser1.setEnabled(true);
            teacherUser1.setAccountNonLocked(true);
            teacherUser1.setTeacher(teacher1);
            userRepository.save(teacherUser1);

            User teacherUser2 = new User();
            teacherUser2.setUsername("teacher2");
            teacherUser2.setPassword(passwordEncoder.encode("password123"));
            teacherUser2.setRole(User.Role.ROLE_TEACHER);
            teacherUser2.setEnabled(true);
            teacherUser2.setAccountNonLocked(true);
            teacherUser2.setTeacher(teacher2);
            userRepository.save(teacherUser2);

            // Create sample courses
            Course course1 = new Course();
            course1.setTitle("Introduction to Programming");
            course1.setDescription("Learn the basics of programming");
            course1.setDepartment(csDept);
            course1.setTeacher(teacher1);
            courseRepository.save(course1);

            Course course2 = new Course();
            course2.setTitle("Data Structures");
            course2.setDescription("Advanced data structures and algorithms");
            course2.setDepartment(csDept);
            course2.setTeacher(teacher1);
            courseRepository.save(course2);

            System.out.println("\n===========================================");
            System.out.println("✅ Sample data created successfully!");
            System.out.println("===========================================");
            System.out.println("\nLOGIN CREDENTIALS:");
            System.out.println("\nSTUDENT ACCOUNTS:");
            System.out.println("  Username: student1  |  Password: password123");
            System.out.println("  Username: student2  |  Password: password123");
            System.out.println("\nTEACHER ACCOUNTS:");
            System.out.println("  Username: teacher1  |  Password: password123");
            System.out.println("  Username: teacher2  |  Password: password123");
            System.out.println("\n===========================================\n");
        };
    }
}

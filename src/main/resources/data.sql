-- Sample data for Student Management System
-- This file will be automatically executed by Spring Boot on startup

-- Insert Departments
INSERT INTO departments (id, name) VALUES (1, 'Computer Science');
INSERT INTO departments (id, name) VALUES (2, 'Mathematics');
INSERT INTO departments (id, name) VALUES (3, 'Physics');

-- Insert Students
INSERT INTO students (id, name, email, department_id) VALUES (1, 'John Doe', 'john.doe@student.edu', 1);
INSERT INTO students (id, name, email, department_id) VALUES (2, 'Jane Smith', 'jane.smith@student.edu', 1);
INSERT INTO students (id, name, email, department_id) VALUES (3, 'Bob Johnson', 'bob.johnson@student.edu', 2);

-- Insert Teachers
INSERT INTO teachers (id, name, email, department_id) VALUES (1, 'Dr. Alice Brown', 'alice.brown@teacher.edu', 1);
INSERT INTO teachers (id, name, email, department_id) VALUES (2, 'Prof. Charlie Wilson', 'charlie.wilson@teacher.edu', 2);

-- Insert Users with BCrypt hashed passwords
-- Password for all users is: "password123"
-- BCrypt hash: $2a$10$rJ1imG2K5j6J5kXJ0xJ5xO5VZQYjXJPXYXJJXJJXJJXJJJJJJJJJJO

-- Student users (ROLE_STUDENT)
INSERT INTO users (id, username, password, role, enabled, account_non_locked, student_id, teacher_id) 
VALUES (1, 'student1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIcf6Z7KmFnqHbZxJrqcVPdCqB8Jy6C2', 'ROLE_STUDENT', true, true, 1, null);

INSERT INTO users (id, username, password, role, enabled, account_non_locked, student_id, teacher_id) 
VALUES (2, 'student2', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIcf6Z7KmFnqHbZxJrqcVPdCqB8Jy6C2', 'ROLE_STUDENT', true, true, 2, null);

INSERT INTO users (id, username, password, role, enabled, account_non_locked, student_id, teacher_id) 
VALUES (3, 'student3', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIcf6Z7KmFnqHbZxJrqcVPdCqB8Jy6C2', 'ROLE_STUDENT', true, true, 3, null);

-- Teacher users (ROLE_TEACHER)
INSERT INTO users (id, username, password, role, enabled, account_non_locked, student_id, teacher_id) 
VALUES (4, 'teacher1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIcf6Z7KmFnqHbZxJrqcVPdCqB8Jy6C2', 'ROLE_TEACHER', true, true, null, 1);

INSERT INTO users (id, username, password, role, enabled, account_non_locked, student_id, teacher_id) 
VALUES (5, 'teacher2', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIcf6Z7KmFnqHbZxJrqcVPdCqB8Jy6C2', 'ROLE_TEACHER', true, true, null, 2);

-- Insert some sample courses
INSERT INTO courses (id, title, description, department_id, teacher_id) 
VALUES (1, 'Introduction to Programming', 'Learn the basics of programming', 1, 1);

INSERT INTO courses (id, title, description, department_id, teacher_id) 
VALUES (2, 'Data Structures', 'Advanced data structures and algorithms', 1, 1);

INSERT INTO courses (id, title, description, department_id, teacher_id) 
VALUES (3, 'Calculus I', 'Introduction to differential calculus', 2, 2);

-- Enroll students in courses (many-to-many relationship)
INSERT INTO student_courses (student_id, course_id) VALUES (1, 1);
INSERT INTO student_courses (student_id, course_id) VALUES (1, 2);
INSERT INTO student_courses (student_id, course_id) VALUES (2, 1);
INSERT INTO student_courses (student_id, course_id) VALUES (3, 3);

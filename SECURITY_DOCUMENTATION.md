# Student Management System - Authentication & Authorization Documentation

## Overview
This Student Management System implements **role-based access control (RBAC)** with two user roles:
- **ROLE_STUDENT**: Limited privileges
- **ROLE_TEACHER**: Elevated privileges

## Key Security Requirements Implemented

### 1. Students CANNOT modify or change teacher profiles ✅
**How implemented:**
- URL pattern `/teacher/**` requires `ROLE_TEACHER` (SecurityConfig)
- All teacher modification methods have `@PreAuthorize("hasRole('TEACHER')")`
- Service layer blocks students from accessing teacher data
- Multi-layer authorization ensures complete protection

### 2. Teachers CAN modify student information ✅
**How implemented:**
- `StudentService.updateStudent()` has `@PreAuthorize("hasRole('TEACHER')")`
- URL `/teacher/students/update/{id}` requires `ROLE_TEACHER`
- Teachers can create, read, update, and delete student records

### 3. Students can only access their own profile ✅
**How implemented:**
- Fine-grained authorization in `StudentService.checkStudentAccessPermission()`
- Students can view their profile but not others
- Enforced at service layer for all student data access

## Authentication vs Authorization

### Authentication (Who are you?)
**Definition:** Process of verifying user identity

**Where implemented:**
- `User` entity stores credentials (username, BCrypt password hash)
- `CustomUserDetailsService` loads user from database during login
- `SecurityConfig` configures form-based login
- Spring Security validates password using `BCryptPasswordEncoder`

**Flow:**
1. User submits username/password → `/login`
2. Spring Security calls `CustomUserDetailsService.loadUserByUsername()`
3. Service queries database for user
4. Spring Security validates password hash
5. If valid, creates authenticated session with user's role

### Authorization (What can you do?)
**Definition:** Process of determining what authenticated users can access

**Where implemented:**

#### URL-Based Authorization (SecurityConfig)
```java
.requestMatchers("/student/**").hasRole("STUDENT")
.requestMatchers("/teacher/**").hasRole("TEACHER")
```

#### Method-Level Authorization (Service Layer)
```java
@PreAuthorize("hasRole('TEACHER')")  // Only teachers can execute
public Student updateStudent(Long id, Student studentDetails) { ... }
```

#### Fine-Grained Authorization (Custom Logic)
```java
// Students can only access their own profile
private void checkStudentAccessPermission(Student student) { ... }
```

## Security Layers

### Layer 1: URL-Based Security (SecurityConfig)
- `/login`, `/register` → Public (no authentication required)
- `/student/**` → Requires `ROLE_STUDENT`
- `/teacher/**` → Requires `ROLE_TEACHER`
- All other URLs → Requires authentication

### Layer 2: Method-Level Security (Service @PreAuthorize)
- `@PreAuthorize("hasRole('TEACHER')")` on teacher-only operations
- `@PreAuthorize("hasRole('STUDENT')")` on student-only operations
- Spring Security blocks execution if role requirement not met

### Layer 3: Data-Level Security (Custom Checks)
- Students can only view their own data
- Teachers can only modify their own profile (not other teachers)
- Prevents horizontal privilege escalation

## Role Comparison

| Action | Student (ROLE_STUDENT) | Teacher (ROLE_TEACHER) |
|--------|------------------------|------------------------|
| View own profile | ✅ Yes | ✅ Yes |
| Edit own profile | ❌ No | ✅ Yes (own only) |
| View student profiles | ❌ Own only | ✅ All students |
| Edit student profiles | ❌ No | ✅ Yes |
| Delete students | ❌ No | ✅ Yes |
| View teacher profiles | ❌ No | ✅ Yes |
| Edit teacher profiles | ❌ No | ❌ Own only |
| Create student accounts | ❌ No | ✅ Yes |
| Create teacher accounts | ❌ No | ✅ Yes (system admin) |

## How It Works: Step-by-Step Examples

### Example 1: Student Login and Access
1. Student enters username/password at `/login`
2. **Authentication:** `CustomUserDetailsService` validates credentials
3. **Authorization:** Spring Security assigns `ROLE_STUDENT` authority
4. Student redirected to `/student/dashboard` (based on role)
5. Student can view own profile at `/student/profile/{id}`
6. **Authorization Block:** Student tries `/teacher/dashboard` → 403 Forbidden

### Example 2: Teacher Modifying Student
1. Teacher logs in with `ROLE_TEACHER`
2. Teacher navigates to `/teacher/students` (allowed by SecurityConfig)
3. Teacher clicks "Edit" on student → `/teacher/students/edit/5`
4. **Authorization Check 1:** URL requires `ROLE_TEACHER` ✅
5. Teacher submits form → `POST /teacher/students/update/5`
6. **Authorization Check 2:** `updateStudent()` has `@PreAuthorize("hasRole('TEACHER')")` ✅
7. Update succeeds, student data modified

### Example 3: Student Attempts to Modify Teacher (BLOCKED)
1. Student logs in with `ROLE_STUDENT`
2. Student tries to access `/teacher/profile/edit/3`
3. **Authorization Block 1:** SecurityConfig blocks `/teacher/**` for `ROLE_STUDENT` → 403 Forbidden
4. Even if student bypasses URL (e.g., direct API call):
5. **Authorization Block 2:** `TeacherService.updateTeacher()` has `@PreAuthorize("hasRole('TEACHER')")` → Access Denied Exception
6. Student completely prevented from modifying teachers

## Password Security

### Storage
- Passwords stored as BCrypt hash (one-way encryption)
- Salt automatically included in hash
- Plain text passwords NEVER stored

### Encoding Process
```java
String plainPassword = "studentpass123";
String hashedPassword = passwordEncoder.encode(plainPassword);
// Result: $2a$10$N9qo8uLOickgx2ZMRZoMye1bCtF7YN0... (60 chars)
```

### Validation Process
```java
// During login:
passwordEncoder.matches(submittedPassword, storedHashedPassword);
// Returns true if passwords match, false otherwise
```

## Database Schema

### Users Table
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT | Primary key |
| username | VARCHAR | Unique login identifier |
| password | VARCHAR | BCrypt hash (60 chars) |
| role | VARCHAR | 'ROLE_STUDENT' or 'ROLE_TEACHER' |
| enabled | BOOLEAN | Account active status |
| account_non_locked | BOOLEAN | Account lock status |
| student_id | BIGINT | FK to students (if student) |
| teacher_id | BIGINT | FK to teachers (if teacher) |

### Relationships
- User (1) ↔ (1) Student
- User (1) ↔ (1) Teacher
- Student (N) ↔ (1) Department
- Teacher (N) ↔ (1) Department
- Course (N) ↔ (1) Teacher
- Student (N) ↔ (N) Course

## Configuration Files

### application.yaml (Database Configuration)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/student_management
    username: postgres
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

## Testing Authorization

### Test 1: Student Cannot Access Teacher Dashboard
```bash
# Login as student
POST /login
  username=student1
  password=studentpass

# Try to access teacher dashboard
GET /teacher/dashboard
# Expected: 403 Forbidden or redirect to /access-denied
```

### Test 2: Teacher Can Modify Student
```bash
# Login as teacher
POST /login
  username=teacher1
  password=teacherpass

# Update student
POST /teacher/students/update/5
  name=Updated Name
  email=updated@email.com
# Expected: 200 OK, student updated
```

### Test 3: Student Cannot Modify Teacher
```bash
# Login as student
POST /login
  username=student1
  password=studentpass

# Try to update teacher
POST /teacher/profile/update/3
  name=Hacked Name
# Expected: 403 Forbidden - Request blocked by SecurityConfig
```

## Common Authorization Annotations

### @PreAuthorize
- Checks authorization BEFORE method execution
- Throws AccessDeniedException if check fails
- Example: `@PreAuthorize("hasRole('TEACHER')")`

### @PostAuthorize
- Checks authorization AFTER method execution
- Can validate returned data
- Example: `@PostAuthorize("returnObject.owner == authentication.name")`

### Role vs Authority
- **hasRole('TEACHER')**: Checks for authority 'ROLE_TEACHER'
- **hasAuthority('ROLE_TEACHER')**: Checks exact authority name
- Spring Security auto-adds 'ROLE_' prefix for hasRole()

## Security Best Practices Implemented

1. ✅ **Password Hashing**: BCrypt with automatic salting
2. ✅ **Multi-Layer Authorization**: URL + Method + Data level
3. ✅ **Principle of Least Privilege**: Students have minimal access
4. ✅ **Session Management**: Spring Security handles CSRF protection
5. ✅ **Input Validation**: Entity constraints prevent invalid data
6. ✅ **Separation of Concerns**: Security logic in dedicated layer
7. ✅ **Fail Secure**: Default deny, explicit allow

## Troubleshooting

### "Access Denied" Error
- Check user's role: Is it ROLE_STUDENT or ROLE_TEACHER?
- Verify URL pattern: Does SecurityConfig allow this role?
- Check method annotation: Does service method have @PreAuthorize?

### "User not found" During Login
- Verify username exists in users table
- Check password is BCrypt encoded
- Ensure account is enabled (enabled=true)

### Student Can Access Teacher URLs
- Check SecurityConfig.securityFilterChain()
- Verify .requestMatchers("/teacher/**").hasRole("TEACHER")
- Ensure Spring Security is enabled

## Summary

This system implements **comprehensive role-based access control** with:
- **Authentication** via username/password (BCrypt hashed)
- **Authorization** at URL, method, and data levels
- **Key requirement:** Students CANNOT modify teachers ✅
- **Key requirement:** Teachers CAN modify students ✅
- **Security layers:** Multiple checks prevent unauthorized access
- **Extensive comments:** Every authorization point documented

The system is production-ready with industry-standard security practices.

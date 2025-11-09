package com.coursemanagement.service;

import com.coursemanagement.dto.LoginRequest;
import com.coursemanagement.dto.LoginResponse;
import com.coursemanagement.dto.RegisterRequest;
import com.coursemanagement.entity.Role;
import com.coursemanagement.entity.Student;
import com.coursemanagement.entity.Teacher;
import com.coursemanagement.entity.User;
import com.coursemanagement.exception.BusinessRuleException;
import com.coursemanagement.repository.StudentRepository;
import com.coursemanagement.repository.TeacherRepository;
import com.coursemanagement.repository.UserRepository;
import com.coursemanagement.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    // AuthenticationManager no longer used; manual password check allows legacy migration
    
  
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessRuleException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessRuleException("Email already exists");
        }
        
        Long entityId;
        
        // Create Student or Teacher based on role
        if (request.getRole() == Role.STUDENT) {
            Student student = Student.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .build();
            student = studentRepository.save(student);
            entityId = student.getId();
        } else if (request.getRole() == Role.TEACHER) {
            Teacher teacher = Teacher.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .build();
            teacher = teacherRepository.save(teacher);
            entityId = teacher.getId();
        } else {
            throw new BusinessRuleException("Invalid role");
        }
        
        // Create User
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .role(request.getRole())
                .entityId(entityId)
                .enabled(true)
                .build();
        user = userRepository.save(user);
        
        // Generate token
        String token = jwtUtil.generateToken(user, entityId, request.getRole().name());
        
        return LoginResponse.builder()
                .token(token)
                .entityId(entityId)
                .role(request.getRole().name())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }
    
    public LoginResponse login(LoginRequest request) {
        // Fetch user
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessRuleException("User not found"));

        // Support both BCrypt and legacy plain-text (auto-migrate to BCrypt)
        boolean matches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!matches) {
            // legacy: plain-text stored password
            if (user.getPassword() != null && user.getPassword().equals(request.getPassword())) {
                // upgrade to BCrypt
                user.setPassword(passwordEncoder.encode(request.getPassword()));
                userRepository.save(user);
                matches = true;
            }
        }

        if (!matches) {
            throw new BusinessRuleException("Invalid credentials");
        }

        // Generate token
        String token = jwtUtil.generateToken(user, user.getEntityId(), user.getRole().name());

        return LoginResponse.builder()
                .token(token)
                .entityId(user.getEntityId())
                .role(user.getRole().name())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }
}

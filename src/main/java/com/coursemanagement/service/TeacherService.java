package com.coursemanagement.service;

import com.coursemanagement.dto.CourseRequest;
import com.coursemanagement.dto.TeacherRequest;
import com.coursemanagement.dto.TeacherResponse;
import com.coursemanagement.entity.Course;
import com.coursemanagement.entity.Teacher;
import com.coursemanagement.exception.BusinessRuleException;
import com.coursemanagement.exception.ResourceNotFoundException;
import com.coursemanagement.repository.CourseRepository;
import com.coursemanagement.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherService {
    
    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;

    private Long getCurrentTeacherId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new BusinessRuleException("Kimlik doğrulaması bulunamadı");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof com.coursemanagement.entity.User user) {
            if (user.getRole() != com.coursemanagement.entity.Role.TEACHER) {
                throw new BusinessRuleException("Sadece kendi öğretmen kayıtlarınızı yönetebilirsiniz");
            }
            Long entityId = user.getEntityId();
            if (entityId == null) {
                throw new BusinessRuleException("Öğretmen bilgisi eksik (entityId)");
            }
            return entityId;
        }
        throw new BusinessRuleException("Geçersiz kimlik bilgisi");
    }
    
    public List<TeacherResponse> getTeachers() {
        return teacherRepository.findAll().stream()
                .map(this::toTeacherResponse)
                .collect(Collectors.toList());
    }
    
    public TeacherResponse getTeacherById(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Öğretmen bulunamadı. ID: " + id));
        return toTeacherResponse(teacher);
    }
    
    @Transactional
    public TeacherResponse createTeacher(TeacherRequest request) {
        if (teacherRepository.existsByEmail(request.getEmail())) {
            throw new BusinessRuleException("Bu e-posta adresi ile kayıtlı bir öğretmen zaten mevcut: " + request.getEmail());
        }
        Teacher teacher = new Teacher();
        teacher.setName(request.getName());
        teacher.setEmail(request.getEmail());
        
        Teacher savedTeacher = teacherRepository.save(teacher);
        
        return toTeacherResponse(savedTeacher);
    }
    
    @Transactional
    public TeacherResponse updateTeacher(Long id, TeacherRequest request) {
        // Sadece kendi öğretmen kaydını güncelleyebilir
        Long currentId = getCurrentTeacherId();
        if (!currentId.equals(id)) {
            throw new BusinessRuleException("Sadece kendi profilinizi güncelleyebilirsiniz");
        }
        Teacher teacher = teacherRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Öğretmen bulunamadı. ID: " + id));
                
        if (request.getName() != null) {
            teacher.setName(request.getName());
        }
        if (request.getEmail() != null) {
            teacher.setEmail(request.getEmail());
        }
        
        Teacher updatedTeacher = teacherRepository.save(teacher);
        return toTeacherResponse(updatedTeacher);
    }
    
    @Transactional
    public void deleteTeacher(Long id) {
        // Sadece kendi öğretmen kaydını silebilir (genelde silmek istenmez ama yine de sınırla)
        Long currentId = getCurrentTeacherId();
        if (!currentId.equals(id)) {
            throw new BusinessRuleException("Sadece kendi profilinizi silebilirsiniz");
        }
        Teacher teacher = teacherRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Öğretmen bulunamadı. ID: " + id));
        
        // Öğretmenin dersleri var mı kontrol et
        if (!teacher.getCourses().isEmpty()) {
            throw new BusinessRuleException("Dersi olan öğretmen silinemez. Önce dersleri silin.");
        }
        
        teacherRepository.delete(teacher);
    }
    
    // Öğretmen entity'sini TeacherResponse'a dönüştürme
    private TeacherResponse toTeacherResponse(Teacher teacher) {
        return TeacherResponse.builder()
                .id(teacher.getId())
                .name(teacher.getName())
                .email(teacher.getEmail())
                .build();
    }
    
    @Transactional(readOnly = true)
    public List<Course> getAllCoursesForTeacher(Long teacherId) {
        // Sadece kendi kurslarını listeleyebilir
        Long currentId = getCurrentTeacherId();
        if (!currentId.equals(teacherId)) {
            throw new BusinessRuleException("Sadece kendi kurslarınızı görüntüleyebilirsiniz");
        }
        if (!teacherRepository.existsById(teacherId)) {
            throw new ResourceNotFoundException("Öğretmen bulunamadı. ID: " + teacherId);
        }
        return courseRepository.findByTeacherId(teacherId);
    }
    
    @Transactional
    public Course createCourseForTeacher(Long teacherId, CourseRequest request) {
        // Sadece kendi adına kurs oluşturabilir
        Long currentId = getCurrentTeacherId();
        if (!currentId.equals(teacherId)) {
            throw new BusinessRuleException("Sadece kendi adınıza kurs oluşturabilirsiniz");
        }
        Teacher teacher = teacherRepository.findById(teacherId)
            .orElseThrow(() -> new ResourceNotFoundException("Öğretmen bulunamadı. ID: " + teacherId));
        
        Course course = Course.builder()
            .name(request.getName())
            .description(request.getDescription())
            .price(request.getPrice())
            .maxStudents(request.getMaxStudents())
            .teacher(teacher)
            .build();
        if (request.getIsAvailable() != null) {
            course.setIsAvailable(request.getIsAvailable());
        }
        
        return courseRepository.save(course);
    }
    
    @Transactional
    public Course updateCourseForTeacher(Long teacherId, Long courseId, CourseRequest request) {
        // Sadece kendi kurslarını güncelleyebilir
        Long currentId = getCurrentTeacherId();
        if (!currentId.equals(teacherId)) {
            throw new BusinessRuleException("Sadece kendi kurslarınızı güncelleyebilirsiniz");
        }
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("Ders bulunamadı. ID: " + courseId));
        
        if (!course.getTeacher().getId().equals(teacherId)) {
            throw new BusinessRuleException("Bu ders belirtilen öğretmene ait değil. Öğretmen ID: " + teacherId);
        }
        
        Teacher teacher = teacherRepository.findById(teacherId)
            .orElseThrow(() -> new ResourceNotFoundException("Öğretmen bulunamadı. ID: " + teacherId));
        
        course.setName(request.getName());
        course.setDescription(request.getDescription());
        course.setPrice(request.getPrice());
        course.setMaxStudents(request.getMaxStudents());
        course.setTeacher(teacher);
        if (request.getIsAvailable() != null) {
            // If capacity is full, availability cannot be forced open
            if (Boolean.TRUE.equals(request.getIsAvailable()) && course.getCurrentStudentCount() >= course.getMaxStudents()) {
                // keep it closed due to capacity
            } else {
                course.setIsAvailable(request.getIsAvailable());
            }
        }
        
        return courseRepository.save(course);
    }
    
    @Transactional
    public void deleteCourseForTeacher(Long teacherId, Long courseId) {
        // Sadece kendi kurslarını silebilir
        Long currentId = getCurrentTeacherId();
        if (!currentId.equals(teacherId)) {
            throw new BusinessRuleException("Sadece kendi kurslarınızı silebilirsiniz");
        }
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("Ders bulunamadı. ID: " + courseId));
        
        if (!course.getTeacher().getId().equals(teacherId)) {
            throw new BusinessRuleException("Bu ders belirtilen öğretmene ait değil. Öğretmen ID: " + teacherId);
        }
        
        if (course.getCurrentStudentCount() > 0) {
            throw new BusinessRuleException("Kayıtlı öğrencisi olan ders silinemez.");
        }
        
        courseRepository.delete(course);
    }
}
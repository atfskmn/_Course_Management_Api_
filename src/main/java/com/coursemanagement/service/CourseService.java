package com.coursemanagement.service;

import com.coursemanagement.entity.Course;
import com.coursemanagement.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService {
    
    private final CourseRepository courseRepository;
    
    public List<Course> getCoursesForStudent(Long studentId) {
        return courseRepository.findEnrolledCoursesByStudentId(studentId);
    }
    
    public List<Course> getAvailableCourses() {
        return courseRepository.findAvailableCourses();
    }
    
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }
}
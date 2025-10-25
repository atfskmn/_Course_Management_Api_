package com.coursemanagement.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coursemanagement.repository.StudentRepository;
import com.coursemanagement.repository.TeacherRepository;
import com.coursemanagement.repository.CourseRepository;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StatsController {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        Map<String, Long> result = new HashMap<>();
        result.put("students", studentRepository.count());
        result.put("teachers", teacherRepository.count());
        result.put("courses", courseRepository.count());
        return ResponseEntity.ok(result);
    }
}

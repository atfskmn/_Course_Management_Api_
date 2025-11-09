package com.coursemanagement.controller;

import com.coursemanagement.dto.StudentRequest;
import com.coursemanagement.dto.StudentResponse;
import com.coursemanagement.entity.Course;
import com.coursemanagement.entity.Order;
import com.coursemanagement.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {
    
    private final StudentService studentService;
    
    @PostMapping
    public ResponseEntity<StudentResponse> registerStudent(@Valid @RequestBody StudentRequest request) {
        return ResponseEntity.ok(studentService.registerStudent(request));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> getStudent(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudent(id));
    }
    
    @GetMapping
    public ResponseEntity<List<StudentResponse>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<StudentResponse> updateStudent(
            @PathVariable Long id, 
            @Valid @RequestBody StudentRequest request) {
        return ResponseEntity.ok(studentService.updateStudent(id, request));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{studentId}/courses")
    public ResponseEntity<List<Course>> getCoursesForStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(studentService.getCoursesForStudent(studentId));
    }
    
    @PostMapping("/{studentId}/orders")
    public ResponseEntity<Order> placeOrder(@PathVariable Long studentId) {
        return ResponseEntity.ok(studentService.placeOrder(studentId));
    }
    
    @GetMapping("/{studentId}/orders")
    public ResponseEntity<List<Order>> getAllOrdersForCustomer(@PathVariable Long studentId) {
        return ResponseEntity.ok(studentService.getAllOrdersForCustomer(studentId));
    }
    
    @GetMapping("/orders/{orderCode}")
    public ResponseEntity<Order> getOrderForCode(@PathVariable String orderCode) {
        return ResponseEntity.ok(studentService.getOrderForCode(orderCode));
    }
}
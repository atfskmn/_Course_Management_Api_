package com.coursemanagement.service;

import com.coursemanagement.dto.StudentRequest;
import com.coursemanagement.dto.StudentResponse;
import com.coursemanagement.entity.*;
import com.coursemanagement.exception.BusinessRuleException;
import com.coursemanagement.exception.ResourceNotFoundException;
import com.coursemanagement.repository.CartRepository;
import com.coursemanagement.repository.OrderRepository;
import com.coursemanagement.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentService {
    
    private final StudentRepository studentRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    
    public StudentService(StudentRepository studentRepository, 
                         CartRepository cartRepository,
                         OrderRepository orderRepository) {
        this.studentRepository = studentRepository;
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
    }
    
    public StudentResponse registerStudent(StudentRequest request) {
        if (studentRepository.existsByEmail(request.getEmail())) {
            throw new BusinessRuleException("Student with email " + request.getEmail() + " already exists");
        }
        
        Student student = new Student();
        student.setName(request.getName());
        student.setEmail(request.getEmail());
        
        Student savedStudent = studentRepository.save(student);
        
        // Create cart for student
        Cart cart = Cart.builder()
            .student(savedStudent)
            .totalPrice(0.0)
            .build();
        
        cartRepository.save(cart);
        savedStudent.setCart(cart);
        
        return toStudentResponse(savedStudent);
    }
    
    public List<StudentResponse> getAllStudents() {
        return studentRepository.findAll().stream()
            .map(this::toStudentResponse)
            .collect(Collectors.toList());
    }
    
    private StudentResponse toStudentResponse(Student student) {
        return StudentResponse.builder()
            .id(student.getId())
            .name(student.getName())
            .email(student.getEmail())
            .build();
    }
    
    public StudentResponse getStudent(Long id) {
        Student student = studentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
        return toStudentResponse(student);
    }
    
    @Transactional
    public StudentResponse updateStudent(Long id, StudentRequest request) {
        Student student = studentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
        
        // Check if email is being changed and if it's already taken
        if (!student.getEmail().equals(request.getEmail()) && 
            studentRepository.existsByEmail(request.getEmail())) {
            throw new BusinessRuleException("Email " + request.getEmail() + " is already taken");
        }
        
        student.setName(request.getName());
        student.setEmail(request.getEmail());
        
        Student updatedStudent = studentRepository.save(student);
        return toStudentResponse(updatedStudent);
    }
    
    @Transactional
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
        
        // Check if student has any orders or enrolled courses
        if (!student.getOrders().isEmpty() || !student.getEnrolledCourses().isEmpty()) {
            throw new BusinessRuleException("Cannot delete student with existing orders or course enrollments");
        }
        
        studentRepository.delete(student);
    }
    
    @Transactional(readOnly = true)
    public List<Course> getCoursesForStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Öğrenci bulunamadı. ID: " + studentId));
        
        return new ArrayList<>(student.getEnrolledCourses());
    }
    
    @Transactional
    public Order placeOrder(Long studentId) {
        // Öğrencinin sepetini al
        Cart cart = cartRepository.findByStudentIdWithItems(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Sepet bulunamadı. Öğrenci ID: " + studentId));
            
        if (cart.getCartItems().isEmpty()) {
            throw new BusinessRuleException("Sipariş oluşturulamadı: Sepet boş");
        }
        
        // Yeni sipariş oluştur
        Order order = new Order();
        order.setStudent(cart.getStudent());
        // Order sınıfındaki status alanı Order.OrderStatus tipinde olduğu için doğrudan atama yapıyoruz
        order.setStatus(Order.OrderStatus.PENDING);
        
        // Sepetteki ürünleri siparişe ekle
        for (CartItem cartItem : cart.getCartItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setCourse(cartItem.getCourse());
            orderItem.setPrice(cartItem.getCourse().getPrice());
            order.addOrderItem(orderItem);
        }
        
        // Siparişi kaydet
        Order savedOrder = orderRepository.save(order);
        
        // Sepeti temizle
        cart.getCartItems().clear();
        cartRepository.save(cart);
        
        return savedOrder;
    }
    
    @Transactional(readOnly = true)
    public Order getOrderForCode(String orderCode) {
        return orderRepository.findByOrderCode(orderCode)
            .orElseThrow(() -> new ResourceNotFoundException("Sipariş bulunamadı. Sipariş Kodu: " + orderCode));
    }
    
    @Transactional(readOnly = true)
    public List<Order> getAllOrdersForCustomer(Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Öğrenci bulunamadı. ID: " + studentId);
        }
        return orderRepository.findByStudentIdWithItems(studentId);
    }
}
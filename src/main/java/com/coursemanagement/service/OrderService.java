package com.coursemanagement.service;

import com.coursemanagement.entity.*;
import com.coursemanagement.exception.BusinessRuleException;
import com.coursemanagement.exception.ResourceNotFoundException;
import com.coursemanagement.repository.CartRepository;
import com.coursemanagement.repository.CourseRepository;
import com.coursemanagement.repository.OrderRepository;
import com.coursemanagement.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    
    @Transactional
    public Order placeOrder(Long studentId) {
        Cart cart = cartRepository.findByStudentIdWithItems(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found for student id: " + studentId));
        
        if (cart.getCartItems().isEmpty()) {
            throw new BusinessRuleException("Cart is empty");
        }
        
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        
        // Validate all courses can still be enrolled and not already owned
        for (CartItem cartItem : cart.getCartItems()) {
            Course course = cartItem.getCourse();
            if (!course.canEnroll()) {
                throw new BusinessRuleException("Course '" + course.getName() + "' is no longer available for enrollment");
            }
            if (student.getEnrolledCourses().contains(course)) {
                throw new BusinessRuleException("You already own the course: " + course.getName());
            }
        }
        
        // Create order
        Order order = Order.builder()
            .student(student)
            .status(Order.OrderStatus.PENDING)
            .build();
        
        // Create order items and enroll students
        // Collect purchased courses for enrollment list before clearing cart
        for (CartItem cartItem : cart.getCartItems()) {
            Course course = cartItem.getCourse();

            // Enroll student in course (decrement availability)
            if (!course.enrollStudent()) {
                throw new BusinessRuleException("Failed to enroll in course: " + course.getName());
            }
            courseRepository.save(course);

            OrderItem orderItem = OrderItem.builder()
                .order(order)
                .course(course)
                .price(course.getPrice())
                .build();

            order.getOrderItems().add(orderItem);
            // Add to student's enrolled list if not present
            if (!student.getEnrolledCourses().contains(course)) {
                student.getEnrolledCourses().add(course);
            }
        }
        
        order.calculateTotalPrice();
        order.setStatus(Order.OrderStatus.COMPLETED);
        
        Order savedOrder = orderRepository.save(order);

        // Persist student with new enrollments
        studentRepository.save(student);

        // Clear cart after enrollments recorded
        cart.clearCart();
        cartRepository.save(cart);
        
        log.info("Order placed with code: {} for student {}", savedOrder.getOrderCode(), studentId);
        return savedOrder;
    }
    
    public Order getOrderForCode(String orderCode) {
        return orderRepository.findByOrderCodeWithItems(orderCode)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with code: " + orderCode));
    }
    
    public List<Order> getAllOrdersForCustomer(Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found with id: " + studentId);
        }
        return orderRepository.findByStudentIdWithItems(studentId);
    }
}
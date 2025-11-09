package com.coursemanagement.service;

import com.coursemanagement.dto.CartItemRequest;
import com.coursemanagement.dto.CartItemResponse;
import com.coursemanagement.dto.CartRequest;
import com.coursemanagement.dto.CartResponse;
import com.coursemanagement.entity.Cart;
import com.coursemanagement.entity.CartItem;
import com.coursemanagement.entity.Course;
import com.coursemanagement.entity.Student;
import com.coursemanagement.exception.BusinessRuleException;
import com.coursemanagement.exception.ResourceNotFoundException;
import com.coursemanagement.repository.CartItemRepository;
import com.coursemanagement.repository.CartRepository;
import com.coursemanagement.repository.CourseRepository;
import com.coursemanagement.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {
    
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    
    // Not read-only because it may create a new cart for the student on first access
    @Transactional
    public CartResponse getCart(Long studentId) {
        Cart cart = cartRepository.findByStudentIdWithItems(studentId)
            .orElseGet(() -> createCartForStudent(studentId));
        return toCartResponse(cart);
    }
    
    @Transactional
    public CartResponse updateCart(CartRequest request) {
        Long studentId = request.getStudentId();
        Cart cart = cartRepository.findByStudentIdWithItems(studentId)
            .orElseGet(() -> createCartForStudent(studentId));
            
        // Clear existing items
        cart.getCartItems().clear();
        
        // Add new items
        for (CartItemRequest itemRequest : request.getItems()) {
            Course course = courseRepository.findById(itemRequest.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + itemRequest.getCourseId()));
                
            if (!course.canEnroll()) {
                throw new BusinessRuleException("Course '" + course.getName() + "' is not available for enrollment");
            }
            
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setCourse(course);
            cart.getCartItems().add(cartItem);
        }
        cart.calculateTotalPrice();
        Cart updatedCart = cartRepository.save(cart);
        return toCartResponse(updatedCart);
    }
    
    @Transactional
    public CartResponse addCourseToCart(Long studentId, Long courseId) {
        Cart cart = cartRepository.findByStudentIdWithItems(studentId)
            .orElseGet(() -> createCartForStudent(studentId));
        
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
        
        // Prevent adding a course the student already owns
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        boolean alreadyEnrolled = student.getEnrolledCourses().stream()
            .anyMatch(c -> c.getId().equals(courseId));
        if (alreadyEnrolled) {
            throw new BusinessRuleException("You already own this course");
        }
        
        if (!course.canEnroll()) {
            throw new BusinessRuleException("Course '" + course.getName() + "' is not available for enrollment");
        }
        
        if (isCourseInCart(cart, courseId)) {
            throw new BusinessRuleException("Course already in cart");
        }
        
        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setCourse(course);
        
        cart.getCartItems().add(cartItem);
        cart.calculateTotalPrice();
        
        Cart savedCart = cartRepository.save(cart);
        return toCartResponse(savedCart);
    }
    
    private boolean isCourseInCart(Cart cart, Long courseId) {
        return cart.getCartItems().stream()
            .anyMatch(item -> item.getCourse().getId().equals(courseId));
    }
    
    @Transactional
    public CartResponse removeCourseFromCart(Long studentId, Long courseId) {
        Cart cart = cartRepository.findByStudentIdWithItems(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found for student id: " + studentId));
        
        CartItem cartItemToRemove = cart.getCartItems().stream()
            .filter(item -> item.getCourse().getId().equals(courseId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Course not found in cart"));
        cart.getCartItems().remove(cartItemToRemove);
        cart.calculateTotalPrice();
        
        cartItemRepository.delete(cartItemToRemove);
        
        return toCartResponse(cartRepository.save(cart));
    }
    
    @Transactional
    public CartResponse emptyCart(Long studentId) {
        Cart cart = cartRepository.findByStudentIdWithItems(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found for student id: " + studentId));
        
        cart.getCartItems().clear();
        cart.calculateTotalPrice();
        
        return toCartResponse(cartRepository.save(cart));
    }
    
    private Cart createCartForStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Öğrenci bulunamadı. ID: " + studentId));
        
        return cartRepository.save(
            Cart.builder()
                .student(student)
                .totalPrice(0.0)
                .build()
        );
    }
    
    public CartItemResponse toCartItemResponse(CartItem cartItem) {
        Course course = cartItem.getCourse();
        return CartItemResponse.builder()
            .id(cartItem.getId())
            .courseId(course.getId())
            .courseName(course.getName())
            .price(course.getPrice())
            .build();
    }
    
    public CartResponse toCartResponse(Cart cart) {
        return CartResponse.builder()
            .id(cart.getId())
            .studentId(cart.getStudent().getId())
            .items(cart.getCartItems().stream()
                .map(this::toCartItemResponse)
                .collect(Collectors.toList()))
            .totalPrice(cart.getTotalPrice())
            .build();
    }
}
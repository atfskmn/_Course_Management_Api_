package com.coursemanagement.controller;

import com.coursemanagement.dto.CartRequest;
import com.coursemanagement.dto.CartResponse;
import com.coursemanagement.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class CartController {
    
    private final CartService cartService;
    
    @GetMapping("/student/{studentId}")
    public ResponseEntity<CartResponse> getCart(@PathVariable Long studentId) {
        return ResponseEntity.ok(cartService.getCart(studentId));
    }
    
    @PostMapping("/student/{studentId}/courses/{courseId}")
    public ResponseEntity<CartResponse> addCourseToCart(
            @PathVariable Long studentId, 
            @PathVariable Long courseId) {
        return ResponseEntity.ok(cartService.addCourseToCart(studentId, courseId));
    }
    
    @DeleteMapping("/student/{studentId}/courses/{courseId}")
    public ResponseEntity<CartResponse> removeCourseFromCart(
            @PathVariable Long studentId, 
            @PathVariable Long courseId) {
        return ResponseEntity.ok(cartService.removeCourseFromCart(studentId, courseId));
    }
    
    @DeleteMapping("/student/{studentId}/empty")
    public ResponseEntity<CartResponse> emptyCart(@PathVariable Long studentId) {
        return ResponseEntity.ok(cartService.emptyCart(studentId));
    }
    
    @PutMapping
    public ResponseEntity<CartResponse> updateCart(@RequestBody CartRequest request) {
        return ResponseEntity.ok(cartService.updateCart(request));
    }
}
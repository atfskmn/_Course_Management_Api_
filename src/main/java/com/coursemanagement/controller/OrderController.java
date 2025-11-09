package com.coursemanagement.controller;

import com.coursemanagement.entity.Order;
import com.coursemanagement.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class OrderController {
    
    private final OrderService orderService;
    
    @PostMapping("/student/{studentId}/place")
    public ResponseEntity<Order> placeOrder(@PathVariable Long studentId) {
        return ResponseEntity.ok(orderService.placeOrder(studentId));
    }
    
    @GetMapping("/{orderCode}")
    public ResponseEntity<Order> getOrderForCode(@PathVariable String orderCode) {
        return ResponseEntity.ok(orderService.getOrderForCode(orderCode));
    }
    
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Order>> getAllOrdersForCustomer(@PathVariable Long studentId) {
        return ResponseEntity.ok(orderService.getAllOrdersForCustomer(studentId));
    }
}
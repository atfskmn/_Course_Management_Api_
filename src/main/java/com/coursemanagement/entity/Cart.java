package com.coursemanagement.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts", schema = "academicdb")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart extends BaseEntity {
    
    @Column(name = "total_price")
    @Builder.Default
    private Double totalPrice = 0.0;
    
    @JsonBackReference("student-cart")
    @OneToOne
    @JoinColumn(name = "student_id", unique = true)
    private Student student;
    
    @JsonManagedReference("cart-items")
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CartItem> cartItems = new ArrayList<>();
    
    public void calculateTotalPrice() {
        this.totalPrice = cartItems.stream()
            .mapToDouble(item -> item.getCourse().getPrice())
            .sum();
    }
    
    public void addCartItem(CartItem cartItem) {
        if (!cartItems.contains(cartItem)) {
            cartItems.add(cartItem);
            cartItem.setCart(this);
            calculateTotalPrice();
        }
    }
    
    public void removeCartItem(CartItem cartItem) {
        if (cartItems.remove(cartItem)) {
            cartItem.setCart(null);
            calculateTotalPrice();
        }
    }
    
    public void clearCart() {
        cartItems.clear();
        totalPrice = 0.0;
    }
    
    public boolean containsCourse(Long courseId) {
        return cartItems.stream()
            .anyMatch(item -> item.getCourse().getId().equals(courseId));
    }
    
    public CartItem getCartItemByCourseId(Long courseId) {
        return cartItems.stream()
            .filter(item -> item.getCourse().getId().equals(courseId))
            .findFirst()
            .orElse(null);
    }
}
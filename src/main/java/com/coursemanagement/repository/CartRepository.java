package com.coursemanagement.repository;

import com.coursemanagement.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByStudentId(Long studentId);
    
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems ci LEFT JOIN FETCH ci.course WHERE c.student.id = :studentId")
    Optional<Cart> findByStudentIdWithItems(@Param("studentId") Long studentId);
}
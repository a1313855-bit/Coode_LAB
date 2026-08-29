package com.example.demo.repository;

// ========== Spring ==========
import org.springframework.data.jpa.repository.JpaRepository;

// ========== Java ==========
import java.util.List;

// ========== Project ==========
import com.example.demo.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // 查詢會員的所有訂單
    List<Order> findByUser_UserId(Long userId);
}

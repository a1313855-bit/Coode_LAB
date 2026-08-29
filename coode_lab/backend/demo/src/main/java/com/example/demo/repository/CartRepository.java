package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Cart;

public interface CartRepository extends JpaRepository<Cart, Long> {

    //1.根據userId查詢購物車
    Optional<Cart> findByUser_UserId(Long userId);
}

package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.CartResponse;
import com.example.demo.service.CartService;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // =========================
    // 根據 cartId 查詢購物車
    // =========================
    @GetMapping("/{cartId}")
    public ResponseEntity<CartResponse> findCartById(
            @PathVariable Long cartId) {

        return cartService.findCartById(cartId)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build());
    }

    // =========================
    // 根據 userId 查詢購物車
    // =========================
    @GetMapping("/user/{userId}")
    public ResponseEntity<CartResponse> findCartByUserId(
            @PathVariable Long userId) {

        return cartService.findCartByUserId(userId)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build());
    }

    // =========================
    // 計算目前有幾種商品
    // =========================
    @GetMapping("/{cartId}/total-quantity")
    public ResponseEntity<Integer> calculateTotalQuantity(
            @PathVariable Long cartId) {

        return ResponseEntity.ok(
                cartService.calculateTotalQuantity(cartId));
    }

    // =========================
    // 重新同步 totalQuantity
    // =========================
    @PatchMapping("/{cartId}/total-quantity")
    public ResponseEntity<CartResponse> updateTotalQuantity(
            @PathVariable Long cartId) {

        return ResponseEntity.ok(
                cartService.updateTotalQuantity(cartId));
    }
}
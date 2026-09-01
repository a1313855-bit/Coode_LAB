package com.example.demo.service;

import java.util.Optional;

import com.example.demo.dto.CartResponse;

public interface CartService {

    // ╔═══════════════════╗
    // ║ 購物車 CRUD 操作    ║
    // ╚═══════════════════╝

    // 根據ID查詢購物車
    Optional<CartResponse> findCartById(Long cartId);

    // 根據userId查詢購物車
    Optional<CartResponse> findCartByUserId(Long userId);

    //計算購物車目前有幾種商品
    Integer calculateTotalQuantity(Long cartId);

    //重新計算並更新 totalQuantity
    CartResponse updateTotalQuantity(Long cartId);

    // ╔═══════════════════╗
    // ║GPT建議只需要查詢即可 ║
    // ╚═══════════════════╝

    // 新增購物車（註冊時自動建立空購物車）
    //Cart createCart(Cart cart);

    // 更新購物車（例如更新totalQuantity）
    //Cart updateCart(Long cartId, Cart cart);

    // 刪除購物車
    //void deleteCart(Long cartId);
}

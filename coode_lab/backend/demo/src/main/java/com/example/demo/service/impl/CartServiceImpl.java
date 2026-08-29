package com.example.demo.service.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.CartResponse;
import com.example.demo.model.Cart;
import com.example.demo.repository.CartItemRepository;
import com.example.demo.repository.CartRepository;
import com.example.demo.service.CartService;

@Service
public class CartServiceImpl implements CartService {

    // ╔═══════════════╗
    // ║ 依賴注入 ║
    // ╚═══════════════╝

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public CartServiceImpl(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository) {

        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    // ╔═══════════════════════╗
    // ║ 購物車 查詢 READ ║
    // ╚═══════════════════════╝

    // 根據 cartId 查詢購物車
    @Override
    public Optional<CartResponse> findCartById(Long cartId) {

        return cartRepository.findById(cartId)
                .map(this::toCartResponse);
    }

    // 根據 userId 查詢購物車
    @Override
    public Optional<CartResponse> findCartByUserId(Long userId) {

        return cartRepository.findByUser_UserId(userId)
                .map(this::toCartResponse);
    }

    // 計算購物車目前有幾種商品
    @Override
    public Integer calculateTotalQuantity(Long cartId) {

        // 1. 先確認購物車是否存在
        if (!cartRepository.existsById(cartId)) {
            throw new IllegalArgumentException(
                    "找不到購物車，cartId: " + cartId);
        }

        // 2. 計算該購物車中的 CartItem 筆數
        Long count = cartItemRepository.countByCart_CartId(cartId);

        // 3. Long 轉 Integer 後回傳
        return count.intValue();
    }

    // 重新計算並更新 totalQuantity
    @Override
    @Transactional
    public CartResponse updateTotalQuantity(Long cartId) {

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("找不到購物車"));

        Long count = cartItemRepository.countByCart_CartId(cartId);

        cart.setTotalQuantity(count.intValue());

        Cart savedCart = cartRepository.save(cart);

        return toCartResponse(savedCart);
    }

    // ╔═══════════════════════╗
    // ║ Entity → Response DTO ║
    // ╚═══════════════════════╝

    private CartResponse toCartResponse(Cart cart) {

        CartResponse response = new CartResponse();

        response.setCartId(cart.getCartId());
        response.setUserId(cart.getUser().getUserId());
        response.setTotalQuantity(cart.getTotalQuantity());
        response.setCreatedAt(cart.getCreatedAt());
        response.setUpdatedAt(cart.getUpdatedAt());

        return response;
    }
}
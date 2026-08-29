package com.example.demo.service;

import java.util.List;

import com.example.demo.dto.AddCartItemRequest;
import com.example.demo.dto.CartItemResponse;
import com.example.demo.dto.UpdateCartItemRequest;

public interface CartItemService {

    // ╔══════════════════════════════╗
    // ║ 購物車商品 查詢 READ         ║
    // ╚══════════════════════════════╝

    // 查詢購物車全部商品
    List<CartItemResponse> findCartItemsByCartId(Long cartId);

    // 查詢購物車某商品
    CartItemResponse findCartItemByCartIdAndProductId(
            Long cartId,
            Long productId);

    // 計算購物車有幾種商品
    Long countDistinctProducts(Long cartId);

    // 依名稱關鍵字搜尋購物車商品
    List<CartItemResponse> findCartItemsByKeyword(
            Long cartId,
            String keyword);

    // ╔══════════════════════════════╗
    // ║ 購物車商品 CREATE            ║
    // ╚══════════════════════════════╝

    // 加入商品
    CartItemResponse addCartItem(AddCartItemRequest request);

    // ╔══════════════════════════════╗
    // ║ 購物車商品 UPDATE            ║
    // ╚══════════════════════════════╝

    // 修改商品數量
    CartItemResponse updateCartItem(
            Long cartItemId,
            UpdateCartItemRequest request);

    // ╔══════════════════════════════╗
    // ║ 購物車商品 DELETE            ║
    // ╚══════════════════════════════╝

    // 刪除購物車某商品
    void deleteCartItem(Long cartId, Long productId);

    // 清空購物車
    void clearCart(Long cartId);
}
package com.example.demo.service;

import java.util.List;

import com.example.demo.dto.AddCartItemRequest;
import com.example.demo.dto.CartItemResponse;
import com.example.demo.dto.UpdateCartItemRequest;
import com.example.demo.util.SelectPartOfData;

public interface CartItemService {

    // ╔══════════════════════════════╗
    // ║ 購物車商品 查詢 READ         ║
    // ╚══════════════════════════════╝

    // 查詢購物車全部商品 (固定每頁10筆,page 從 0 開始)
    SelectPartOfData.Result<CartItemResponse> findCartItemsByCartId(Long cartId, int page);

    // 查詢購物車某規格
    CartItemResponse findCartItemByCartIdAndVariantId(
            Long cartId,
            Long variantId);

    // 計算購物車有幾種商品
    Long countDistinctProducts(Long cartId);

    // 依名稱關鍵字搜尋購物車商品 (固定每頁10筆,page 從 0 開始)
    SelectPartOfData.Result<CartItemResponse> findCartItemsByKeyword(
            Long cartId,
            String keyword,
            int page);

    // ╔══════════════════════════════╗
    // ║ 購物車商品 CREATE            ║
    // ╚══════════════════════════════╝

    // 加入商品（以規格為單位）
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

    // 刪除購物車某規格
    void deleteCartItem(Long cartId, Long variantId);

    // 清空購物車
    void clearCart(Long cartId);
}
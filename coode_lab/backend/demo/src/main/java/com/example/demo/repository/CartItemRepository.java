package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.CartItem;

public interface CartItemRepository
        extends JpaRepository<CartItem, Long> {

    // 1.查詢購物車全部商品
    List<CartItem> findByCart_CartId(Long cartId);

    // 2.查詢購物車是否已存在某商品
    Optional<CartItem> findByCart_CartIdAndProduct_ProductId(
            Long cartId,
            Long productId);

    // 3.刪除購物車裡某商品
    void deleteByCart_CartIdAndProduct_ProductId(
            Long cartId,
            Long productId);

    // 4.清空購物車
    void deleteByCart_CartId(Long cartId);

    // 5.計算購物車裡有幾種商品
    Long countByCart_CartId(Long cartId);

    // 6.依商品名稱關鍵字搜尋購物車商品
    List<CartItem> findByCart_CartIdAndProduct_NameContaining(Long cartId,String keyword);
}
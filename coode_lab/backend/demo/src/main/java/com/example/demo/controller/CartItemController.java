package com.example.demo.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.AddCartItemRequest;
import com.example.demo.dto.CartItemResponse;
import com.example.demo.dto.UpdateCartItemRequest;
import com.example.demo.service.CartItemService;
import com.example.demo.util.SelectPartOfData;

@RestController
@RequestMapping("/api/cart-items")
public class CartItemController {

    private final CartItemService cartItemService;

    public CartItemController(
            CartItemService cartItemService) {

        this.cartItemService = cartItemService;
    }

    // =========================
    // 查詢購物車全部商品
    // =========================
    @GetMapping("/cart/{cartId}")
    public ResponseEntity<SelectPartOfData.Result<CartItemResponse>>
            findCartItemsByCartId(
                    @PathVariable Long cartId,
                    @RequestParam(defaultValue = "0") int page) {

        SelectPartOfData.Result<CartItemResponse> items =
                cartItemService
                        .findCartItemsByCartId(cartId, page);

        return ResponseEntity.ok(items);
    }

    // =========================
    // 查詢購物車指定規格
    // =========================
    @GetMapping("/cart/{cartId}/variant/{variantId}")
    public ResponseEntity<CartItemResponse>
            findCartItemByCartIdAndVariantId(
                    @PathVariable Long cartId,
                    @PathVariable Long variantId) {

        CartItemResponse item =
                cartItemService
                        .findCartItemByCartIdAndVariantId(
                                cartId,
                                variantId);

        return ResponseEntity.ok(item);
    }

    // =========================
    // 查詢購物車商品種類數
    // =========================
    @GetMapping("/cart/{cartId}/count")
    public ResponseEntity<Long> countDistinctProducts(
            @PathVariable Long cartId) {

        Long count =
                cartItemService
                        .countDistinctProducts(cartId);

        return ResponseEntity.ok(count);
    }

    // =========================
    // 關鍵字搜尋
    // =========================
    @GetMapping("/cart/{cartId}/search")
    public ResponseEntity<SelectPartOfData.Result<CartItemResponse>>
            findCartItemsByKeyword(
                    @PathVariable Long cartId,
                    @RequestParam String keyword,
                    @RequestParam(defaultValue = "0") int page) {

        SelectPartOfData.Result<CartItemResponse> items =
                cartItemService
                        .findCartItemsByKeyword(
                                cartId,
                                keyword,
                                page);

        return ResponseEntity.ok(items);
    }

    // =========================
    // 加入購物車
    // =========================
    @PostMapping
    public ResponseEntity<CartItemResponse>
            addCartItem(
                    @Valid @RequestBody AddCartItemRequest request) {

        CartItemResponse addedItem =
                cartItemService.addCartItem(request);

        return ResponseEntity.ok(addedItem);
    }

    // =========================
    // 修改商品件數
    // =========================
    @PatchMapping("/{cartItemId}")
    public ResponseEntity<CartItemResponse>
            updateCartItem(
                    @PathVariable Long cartItemId,
                    @Valid @RequestBody UpdateCartItemRequest request) {

        CartItemResponse updatedItem =
                cartItemService.updateCartItem(
                        cartItemId,
                        request);

        return ResponseEntity.ok(updatedItem);
    }

    // =========================
    // 刪除一種規格商品
    // =========================
    @DeleteMapping("/cart/{cartId}/variant/{variantId}")
    public ResponseEntity<Void> deleteCartItem(
            @PathVariable Long cartId,
            @PathVariable Long variantId) {

        cartItemService.deleteCartItem(
                cartId,
                variantId);

        return ResponseEntity.noContent().build();
    }

    // =========================
    // 清空購物車
    // =========================
    @DeleteMapping("/cart/{cartId}")
    public ResponseEntity<Void> clearCart(
            @PathVariable Long cartId) {

        cartItemService.clearCart(cartId);

        return ResponseEntity.noContent().build();
    }
}
package com.example.demo.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemResponse {

    private Long cartItemId;

    private Long cartId;

    private Long productId;

    private String productName;

    private Integer productQuantity;

    // 商品目前即時庫存（前端用來做連動庫存檢查）
    private Integer stock;

    private BigDecimal price;

    private BigDecimal totalPrice;
}
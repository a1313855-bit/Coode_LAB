package com.example.demo.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemResponse {

    private Long cartItemId;

    private Long cartId;

    private Long variantId;

    // 規格的顏色／尺寸（會員實際購買的規格）
    private String color;

    private String size;

    private Long productId;

    private String productName;

    // 商品目前總狀態（ACTIVE/INACTIVE），供前端判斷整件停售
    private String productStatus;

    // 規格目前販售狀態（ACTIVE/INACTIVE），停售的規格不能結帳
    private String variantStatus;

    private Integer productQuantity;

    // 規格目前即時庫存（前端用來做連動庫存檢查）
    private Integer stock;

    private BigDecimal price;

    private BigDecimal totalPrice;
}
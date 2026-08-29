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

    private BigDecimal price;

    private BigDecimal totalPrice;
}
package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddCartItemRequest {

    private Long cartId;

    private Long productId;

    private Integer productQuantity;
}
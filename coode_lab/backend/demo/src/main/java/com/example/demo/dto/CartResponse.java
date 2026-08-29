package com.example.demo.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartResponse {

    private Long cartId;

    private Long userId;

    private Integer totalQuantity;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
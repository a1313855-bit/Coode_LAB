package com.example.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCartItemRequest {

    @NotNull(message = "商品數量不能為空")
    @Min(value = 1, message = "商品數量必須大於 0")
    private Integer productQuantity;
}
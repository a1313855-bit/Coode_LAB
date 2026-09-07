package com.example.demo.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BatchProductRequest {

    @NotNull(message = "商品清單不能為空")
    @NotEmpty(message = "請至少選擇一個商品")
    private List<@NotNull(message = "商品 ID 不能為空") Long> productIds;
}
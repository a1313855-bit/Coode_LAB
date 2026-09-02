package com.example.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantRequest {

    @NotBlank(message = "商品顏色不能為空")
    private String color;

    @NotBlank(message = "商品尺寸不能為空")
    private String size;

    @NotNull(message = "庫存數量不能為空")
    @Min(value = 0, message = "庫存數量不能小於 0")
    private Integer stock;

    private String imagesJpg;

    private String outfitPng;

    // 此規格組合的販售狀態：ACTIVE = 可販售；INACTIVE = 停售
    private String status;
}
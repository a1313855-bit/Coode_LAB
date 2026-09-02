package com.example.demo.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
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
public class ProductRequest {

    @NotBlank(message = "商品名稱不能為空")
    private String name;

    private String pattern;

    @NotBlank(message = "商品類別不能為空")
    private String categoryType;

    private String gender;

    private String style;

    @NotBlank(message = "商品顏色不能為空")
    private String color;

    @NotBlank(message = "商品尺寸不能為空")
    private String size;

    @NotNull(message = "庫存數量不能為空")
    @Min(value = 0, message = "庫存數量不能小於 0")
    private Integer stock;

    @NotNull(message = "商品價格不能為空")
    @DecimalMin(value = "0", message = "商品價格不能小於 0")
    private BigDecimal price;

    private String description;

    private String imagesJpg;

    private String outfitPng;

    // 新增商品時選擇：
    // ACTIVE = 直接上架
    // DRAFT = 待上架
    private String status;
}
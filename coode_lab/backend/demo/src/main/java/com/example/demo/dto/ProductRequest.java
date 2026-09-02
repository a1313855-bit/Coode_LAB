package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
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

    private String style;

    @NotNull(message = "商品價格不能為空")
    @DecimalMin(value = "0", message = "商品價格不能小於 0")
    private BigDecimal price;

    private String description;

    // 封面圖（商城列表用）
    private String imagesJpg;

    // 商品層級的試穿疊圖；規格未填時回退用此圖
    private String outfitPng;

    // 新增商品時選擇：
    // ACTIVE = 直接上架
    // DRAFT = 待上架
    private String status;

    // 此商品的所有規格（顏色 × 尺寸 × 庫存 × 圖片 × 販售狀態）
    private List<ProductVariantRequest> variants = new ArrayList<>();
}
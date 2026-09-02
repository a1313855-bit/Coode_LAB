package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long productId;

    private String name;

    private String pattern;

    private String categoryType;

    private String style;

    private BigDecimal price;

    private String description;

    // 封面圖（商城列表用）
    private String imagesJpg;

    // 商品層級的試穿疊圖
    private String outfitPng;

    // 商品總開關：ACTIVE 整件販售；INACTIVE 整件停售
    private String status;

    private Long vendorId;

    private String vendorName;

    // 此商品的所有規格
    private List<ProductVariantResponse> variants = new ArrayList<>();
}
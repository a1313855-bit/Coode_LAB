package com.example.demo.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    private String name;

    private String pattern;

    private String categoryType;

    private String style;

    private String color;

    private String size;

    private Integer stock;

    private BigDecimal price;

    private String description;

    private String imagesJpg;

    private String outfitPng;

    // 新增商品時選擇：
    // ACTIVE = 直接上架
    // DRAFT = 待上架
    private String status;
}
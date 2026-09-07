package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantResponse {

    private Long variantId;

    private Long productId;

    private String color;

    private String size;

    private Integer stock;

    private String imagesJpg;

    private String outfitPng;

    private String status;
}
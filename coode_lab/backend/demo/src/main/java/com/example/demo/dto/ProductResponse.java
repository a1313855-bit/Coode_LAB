package com.example.demo.dto;

import java.math.BigDecimal;

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

    private String color;

    private String size;

    private Integer stock;

    private BigDecimal price;

    private String description;

    private String imagesJpg;

    private String outfitPng;

    private String status;

    private Long vendorId;

    private String vendorName;

    private String gender;

}

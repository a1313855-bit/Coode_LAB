package com.example.demo.dto;

// ========== lombok ==========
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 穿搭商品資料回傳格式。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OutfitItemResponse {

    // 穿搭商品明細 ID
    private Long outfitItemId;

    // 所屬穿搭 ID
    private Long outfitId;

    // 商品 ID
    private Long productId;

    // 商品名稱（方便前端直接顯示）
    private String productName;

    // 規格 ID（記錄所選顏色）
    private Long variantId;

    // 規格顏色
    private String color;

    // 規格尺寸
    private String size;

    // 規格商品照（選色的商品照）
    private String variantImagesJpg;

    // 規格試穿疊圖（選色的試穿圖）
    private String variantOutfitPng;

    // 穿搭位置
    private String slotType;
}
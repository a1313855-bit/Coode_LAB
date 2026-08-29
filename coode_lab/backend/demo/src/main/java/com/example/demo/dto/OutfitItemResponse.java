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

    // 穿搭位置
    private String slotType;
}
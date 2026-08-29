package com.example.demo.dto;

// ========== lombok ==========
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 新增或替換穿搭商品時，
 * 前端傳入的資料。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OutfitItemRequest {

    // 商品 ID
    private Long productId;

    // 穿搭位置，例如 TOP、BOTTOM、SHOES
    private String slotType;
}
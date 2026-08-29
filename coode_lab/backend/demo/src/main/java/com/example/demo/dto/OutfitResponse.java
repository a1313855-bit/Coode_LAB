package com.example.demo.dto;

// ========== lombok ==========
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// ========== Java ==========
import java.util.List;

/**
 * 穿搭資料回傳格式。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OutfitResponse {

    // 穿搭 ID
    private Long outfitId;

    // 使用者 ID
    private Long userId;

    // 穿搭名稱
    private String name;

    // 此穿搭包含的商品
    private List<OutfitItemResponse> items;
}
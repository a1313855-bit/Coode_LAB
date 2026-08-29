package com.example.demo.dto;

// ========== lombok ==========
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 建立穿搭時，前端傳入的資料。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OutfitCreateRequest {

    // 使用者 ID
    private Long userId;

    // 穿搭名稱
    private String name;
}
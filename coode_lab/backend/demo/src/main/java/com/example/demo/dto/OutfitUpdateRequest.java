package com.example.demo.dto;

// ========== lombok ==========
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 修改穿搭時，前端傳入的資料。
 *
 * 目前 Outfit 可修改的資料只有穿搭名稱，
 * 因此只需要 name。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OutfitUpdateRequest {

    // 新的穿搭名稱
    private String name;
}
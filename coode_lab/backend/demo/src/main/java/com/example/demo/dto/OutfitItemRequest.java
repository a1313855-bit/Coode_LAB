package com.example.demo.dto;

// ========== jakarta validation ==========
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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
    @NotNull(message = "商品 ID 不能為空")
    private Long productId;

    // 規格 ID（記錄「哪件商品、哪個顏色」，用來顯示對應試穿圖）
    @NotNull(message = "規格 ID 不能為空")
    private Long variantId;

    // 穿搭位置，例如 UPPER_BODY、BOTTOM、FULL_BODY
    @NotBlank(message = "穿搭位置不能為空")
    private String slotType;
}
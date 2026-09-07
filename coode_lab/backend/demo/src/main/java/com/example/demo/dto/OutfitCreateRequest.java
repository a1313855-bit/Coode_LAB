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
 * 建立穿搭時，前端傳入的資料。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OutfitCreateRequest {

    // 使用者 ID
    @NotNull(message = "會員 ID 不能為空")
    private Long userId;

    // 穿搭名稱
    @NotBlank(message = "穿搭名稱不能為空")
    private String name;
}
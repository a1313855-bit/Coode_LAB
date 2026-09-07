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
 * 試衣間儲存穿搭時，前端指定「商品 + 穿搭位置」。
 *
 * 讓 TOP 與 OUTER 可以同時存在（分別放在不同位置）。
 * 同時記錄使用者挑選的規格（顏色），讓試穿圖依顏色顯示。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OutfitItemSlotRequest {

    // 商品 ID
    @NotNull(message = "商品 ID 不能為空")
    private Long productId;

    // 規格 ID（記錄所選顏色）
    @NotNull(message = "規格 ID 不能為空")
    private Long variantId;

    // 穿搭位置：HEADWEAR / UPPER_BODY / BOTTOM / FULL_BODY
    @NotBlank(message = "穿搭位置不能為空")
    private String slotType;
}
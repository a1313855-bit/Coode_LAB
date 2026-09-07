package com.example.demo.dto.returnitem;

// ========== Lombok ==========
import lombok.Getter;
import lombok.Setter;

// ========== Jakarta Validation ==========
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class CreateReturnItemRequest {

    // ╔═══════╗
    // ║ Field ║
    // ╚═══════╝
    @NotNull
    private Long orderItemId;

    @NotNull
    @Min(1)
    private Integer requestQuantity;

    // 退貨商品照片（可選）
    private String picture;
}

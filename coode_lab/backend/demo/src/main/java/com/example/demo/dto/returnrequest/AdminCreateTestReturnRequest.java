package com.example.demo.dto.returnrequest;

// ========== Lombok ==========
import lombok.Getter;
import lombok.Setter;

// ========== Jakarta Validation ==========
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class AdminCreateTestReturnRequest {

    // ╔═══════╗
    // ║ Field ║
    // ╚═══════╝
    @NotNull
    private Long orderItemId;

    @NotBlank
    private String requestType;

    @NotNull
    @Min(1)
    private Integer requestQuantity;

    // 退貨商品照片（可選）
    private String picture;

    // 退換貨原因
    private String reason;
}
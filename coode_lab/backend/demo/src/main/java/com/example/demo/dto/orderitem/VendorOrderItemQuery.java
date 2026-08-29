package com.example.demo.dto.orderitem;

// ========== Lombok ==========
import lombok.Getter;
import lombok.Setter;

// ========== Jakarta Validation ==========
import jakarta.validation.constraints.Pattern;

@Getter
@Setter
public class VendorOrderItemQuery {

    // 廠商想看的狀態群組：未寄出 / 已寄出 / 已完成 / 取消
    // 不帶 = 查看全部（由 Controller 分流到 findItemsByVendorId）
    @Pattern(regexp = "NOT_SHIPPED|IN_TRANSIT|COMPLETED|CANCELLED",
             message = "狀態群組只能是 NOT_SHIPPED, IN_TRANSIT, COMPLETED, CANCELLED")
    private String status;
}
package com.example.demo.dto.orderitem;

// ========== Lombok ==========
import lombok.Getter;
import lombok.Setter;

// ========== Jakarta Validation ==========
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Getter
@Setter
public class UpdateOrderItemStatusRequest {

    // ╔═══════╗
    // ║ Field ║
    // ╚═══════╝
    @NotBlank
    @Pattern(regexp = "PENDING|PROCESSING|SHIPPED|RECEIVED|CANCELLED",
             message = "狀態只能是 PENDING, PROCESSING, SHIPPED, RECEIVED, CANCELLED")
    private String status;
}

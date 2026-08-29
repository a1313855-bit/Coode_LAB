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
    @Pattern(regexp = "PENDING|SHIPPED|ARRIVED|RECEIVED|CANCELLED",
             message = "狀態只能是 PENDING, SHIPPED, ARRIVED, RECEIVED, CANCELLED")
    private String status;
}

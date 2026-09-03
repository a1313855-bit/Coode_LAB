package com.example.demo.dto.orderitem;

// ========== Lombok ==========
import lombok.Getter;
import lombok.Setter;

// ========== Jakarta Validation ==========
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class UpdateOrderItemRequest {

    // ╔═══════╗
    // ║ Field ║
    // ╚═══════╝

    @Min(1)
    private Integer productQuantity;

    private String status;
}

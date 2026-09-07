package com.example.demo.dto.order;

// ========== Lombok ==========
import lombok.Getter;
import lombok.Setter;

// ========== Jakarta Validation ==========
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// ========== Java ==========
import java.util.List;

@Getter
@Setter
public class CreateOrderRequest {

    // ╔═══════╗
    // ║ Field ║
    // ╚═══════╝
    @NotNull
    private Long userId;

    @NotNull
    private List<Long> cartItemIds;

    @NotBlank
    private String recipientName;

    @NotBlank
    private String recipientPhone;

    @NotBlank
    private String recipientAddress;
}

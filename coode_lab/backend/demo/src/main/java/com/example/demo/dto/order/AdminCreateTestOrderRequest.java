package com.example.demo.dto.order;

// ========== Lombok ==========
import lombok.Getter;
import lombok.Setter;

// ========== Jakarta Validation ==========
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

// ========== Java ==========
import java.util.List;

@Getter
@Setter
public class AdminCreateTestOrderRequest {

    // ╔═══════╗
    // ║ Field ║
    // ╚═══════╝
    @NotNull
    private Long userId;

    @NotBlank
    private String recipientName;

    @NotBlank
    private String recipientPhone;

    @NotBlank
    private String recipientAddress;

    @NotEmpty
    private List<Item> items;

    @Getter
    @Setter
    public static class Item {

        @NotNull
        private Long variantId;

        @NotNull
        @Min(1)
        private Integer quantity;
    }
}
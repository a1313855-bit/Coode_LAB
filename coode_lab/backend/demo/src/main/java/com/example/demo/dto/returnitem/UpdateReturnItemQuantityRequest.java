package com.example.demo.dto.returnitem;

// ========== Lombok ==========
import lombok.Getter;
import lombok.Setter;

// ========== Jakarta Validation ==========
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class UpdateReturnItemQuantityRequest {

    @NotNull
    @Min(0)
    private Integer approvedQuantity;

    @NotNull
    @Min(0)
    private Integer rejectedQuantity;
}

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
public class CreateReturnRequestRequest {

    // ╔═══════╗
    // ║ Field ║
    // ╚═══════╝
    @NotBlank
    private String requestType;

    @NotNull
    @Min(1)
    private Integer returnRequestQuantity;
}

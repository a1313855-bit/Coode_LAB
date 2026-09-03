package com.example.demo.dto.returnitem;

// ========== Lombok ==========
import lombok.Getter;
import lombok.Setter;

// ========== Jakarta Validation ==========
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Getter
@Setter
public class UpdateReturnItemStatusRequest {

    @NotBlank
    @Pattern(regexp = "PENDING_REVIEW|APPROVED|REJECTED|AWAITING_SHIPBACK|SHIPPED_BACK|RECEIVED|REFUNDING|REFUNDED|EXCHANGING|EXCHANGE_SHIPPED|EXCHANGED|COMPLETED|CANCELLED",
             message = "狀態只能是 PENDING_REVIEW, APPROVED, REJECTED, AWAITING_SHIPBACK, SHIPPED_BACK, RECEIVED, REFUNDING, REFUNDED, EXCHANGING, EXCHANGE_SHIPPED, EXCHANGED, COMPLETED, CANCELLED")
    private String status;
}

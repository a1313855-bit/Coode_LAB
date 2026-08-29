package com.example.demo.dto.returnrequest;

// ========== Lombok ==========
import lombok.Getter;
import lombok.Setter;

// ========== Jakarta Validation ==========
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Getter
@Setter
public class UpdateReturnRequestStatusRequest {

    // ╔═══════╗
    // ║ Field ║
    // ╚═══════╝
    @NotBlank
    @Pattern(regexp = "PENDING|REVIEWED|CANCELLED",
             message = "狀態只能是 PENDING, REVIEWED, CANCELLED")
    private String status;
}

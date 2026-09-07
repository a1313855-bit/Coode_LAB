package com.example.demo.dto.order;

// ========== Lombok ==========
import lombok.Getter;
import lombok.Setter;

// ========== Jakarta Validation ==========
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
public class UpdateRecipientRequest {

    // ╔═══════╗
    // ║ Field ║
    // ╚═══════╝
    @NotBlank
    private String recipientName;

    @NotBlank
    private String recipientPhone;

    @NotBlank
    private String recipientAddress;
}

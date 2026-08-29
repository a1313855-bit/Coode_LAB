package com.example.demo.dto.order;

// ========== Lombok ==========
import lombok.Getter;
import lombok.Setter;

// ========== Java ==========
import java.math.BigDecimal;
import java.time.LocalDateTime;

// ========== Jackson ==========
import com.fasterxml.jackson.annotation.JsonFormat;

@Getter
@Setter
public class OrderDTO {

    // ╔═══════╗
    // ║ Field ║
    // ╚═══════╝
    private Long orderId;
    private String recipientName;
    private String recipientPhone;
    private String recipientAddress;
    private Integer totalAmount;
    private BigDecimal sumTotal;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private UserInfo user;

    // ╔══════════════════════════════════╗
    // ║ Nested class : 使用者（精簡）   ║
    // ╚══════════════════════════════════╝
    @Getter
    @Setter
    public static class UserInfo {
        private Long userId;
        private String name;
    }
}

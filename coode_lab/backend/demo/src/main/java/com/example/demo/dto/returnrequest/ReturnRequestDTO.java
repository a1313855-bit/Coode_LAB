package com.example.demo.dto.returnrequest;

// ========== Lombok ==========
import lombok.Getter;
import lombok.Setter;

// ========== Jackson ==========
import com.fasterxml.jackson.annotation.JsonFormat;

// ========== Java ==========
import java.time.LocalDateTime;

@Getter
@Setter
public class ReturnRequestDTO {

    // ╔═══════╗
    // ║ Field ║
    // ╚═══════╝
    private Long returnRequestsId;
    private String status;
    private String requestType;
    private Integer returnRequestQuantity;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private OrderInfo order;
    private VendorInfo vendor;
    private UserInfo user;
    private ReturnItemInfo returnItem;

    // ╔══════════════════════════════════╗
    // ║ Nested class : 訂單（精簡）     ║
    // ╚══════════════════════════════════╝
    @Getter
    @Setter
    public static class OrderInfo {
        private Long orderId;
    }

    // ╔══════════════════════════════════╗
    // ║ Nested class : 廠商（精簡）     ║
    // ╚══════════════════════════════════╝
    @Getter
    @Setter
    public static class VendorInfo {
        private Long vendorId;
        private String vendorName;
    }

    // ╔══════════════════════════════════╗
    // ║ Nested class : 使用者（精簡）   ║
    // ╚══════════════════════════════════╝
    @Getter
    @Setter
    public static class UserInfo {
        private Long userId;
        private String name;
    }

    // ╔══════════════════════════════════╗
    // ║ Nested class : 退貨商品（精簡） ║
    // ╚══════════════════════════════════╝
    @Getter
    @Setter
    public static class ReturnItemInfo {
        private Long returnItemId;
        private String status;
        private Long orderItemId;
        private Integer approvalQuantity;
        private Integer rejectedQuantity;
        private String productName;
        private String picture;
        private String categoryType;
        private String color;
        private String size;
        private String pattern;
    }
}

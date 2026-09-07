package com.example.demo.dto.returnitem;

// ========== Lombok ==========
import lombok.Getter;
import lombok.Setter;

// ========== Java ==========
import java.math.BigDecimal;

@Getter
@Setter
public class ReturnItemDTO {

    // ╔═══════╗
    // ║ Field ║
    // ╚═══════╝
    private Long returnItemId;
    private String status;
    private String reason;
    private String description;
    private String picture;
    private Integer approvalQuantity;
    private Integer rejectedQuantity;
    private BigDecimal refund;

    private ReturnRequestInfo returnRequest;
    private OrderItemInfo orderItem;

    // ╔══════════════════════════════════╗
    // ║ Nested class : 退貨申請（精簡） ║
    // ╚══════════════════════════════════╝
    @Getter
    @Setter
    public static class ReturnRequestInfo {
        private Long returnRequestsId;
        private String requestType;
        private Integer returnRequestQuantity;
    }

    // ╔══════════════════════════════════╗
    // ║ Nested class : 訂單明細（精簡） ║
    // ╚══════════════════════════════════╝
    @Getter
    @Setter
    public static class OrderItemInfo {
        private Long orderItemId;
        private Integer productQuantity;
        private BigDecimal price;
        private ProductInfo product;
    }

    // ╔══════════════════════════════════╗
    // ║ Nested class : 商品（精簡）     ║
    // ╚══════════════════════════════════╝
    @Getter
    @Setter
    public static class ProductInfo {
        private Long productId;
        private String name;
        private String pattern;
        private String categoryType;
        private String style;
        private String color;
        private String size;
    }
}

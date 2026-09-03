package com.example.demo.dto.orderitem;

// ========== Lombok ==========
import lombok.Getter;
import lombok.Setter;

// ========== Java ==========
import java.math.BigDecimal;

@Getter
@Setter
public class OrderItemDTO {

    // ╔═══════╗
    // ║ Field ║
    // ╚═══════╝
    private Long orderItemId;
    private Integer productQuantity;
    private BigDecimal price;
    private BigDecimal priceTotal;
    private String status;

    // 會員端判斷是否可申請退換貨（規則由後端統一計算）
    // 訂單明細已完成（RECEIVED）且此訂單無進行中的退換貨申請
    private Boolean canReturnOrExchange;
    // 此訂單目前的退換貨狀態：PROCESSING（進行中）/ COMPLETED（已完成）/ 無則為 null
    private String returnStatus;

    private OrderInfo order;
    private VendorInfo vendor;
    private VariantInfo variant;

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
    // ║ Nested class : 商品規格（精簡） ║
    // ╚══════════════════════════════════╝
    @Getter
    @Setter
    public static class VariantInfo {
        private Long variantId;
        private String color;
        private String size;
        private Integer stock;
        private String status;
        private String imagesJpg;
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
        private BigDecimal price;
    }
}
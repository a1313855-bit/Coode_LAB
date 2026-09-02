package com.example.demo.dto.orderitem;

// ========== Lombok ==========
import lombok.Getter;
import lombok.Setter;

// ========== Java ==========
import java.math.BigDecimal;

@Getter
@Setter
public class OrderItemVendorDTO {

    // ╔═══════╗
    // ║ Field ║
    // ╚═══════╝
    private Long orderItemId;
    private Integer productQuantity;
    private BigDecimal price;
    private BigDecimal priceTotal;
    private String status;

    private OrderInfo order;
    private VendorInfo vendor;
    private VariantInfo variant;

    // ╔══════════════════════════════════════╗
    // ║ Nested class : 訂單（含收件資訊）   ║
    // ╚══════════════════════════════════════╝
    @Getter
    @Setter
    public static class OrderInfo {
        private Long orderId;
        private String recipientName;
        private String recipientPhone;
        private String recipientAddress;
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
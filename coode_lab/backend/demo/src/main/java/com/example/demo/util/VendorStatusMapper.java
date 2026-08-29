package com.example.demo.util;

// ========== Java ==========
import java.util.List;

public final class VendorStatusMapper {

    // 廠商想看的狀態群組
    public static final String NOT_SHIPPED = "NOT_SHIPPED";
    public static final String IN_TRANSIT = "IN_TRANSIT";
    public static final String COMPLETED = "COMPLETED";
    public static final String CANCELLED = "CANCELLED";

    private VendorStatusMapper() {
    }

    // 廠商狀態群組 -> OrderItem 原始狀態（vendorStatus 不進入資料庫，只作為分類）
    public static List<String> toOrderItemStatuses(String vendorStatus) {
        switch (vendorStatus) {
            case NOT_SHIPPED:
                return List.of("PENDING");
            case IN_TRANSIT:
                return List.of("SHIPPED", "ARRIVED");
            case COMPLETED:
                return List.of("RECEIVED");
            case CANCELLED:
                return List.of("CANCELLED");
            default:
                throw new IllegalArgumentException("未知的廠商狀態群組: " + vendorStatus);
        }
    }

    // OrderItem 原始狀態 -> 廠商狀態群組（未知狀態返回 null）
    public static String toVendorStatus(String orderItemStatus) {
        switch (orderItemStatus) {
            case "PENDING":
                return NOT_SHIPPED;
            case "SHIPPED":
            case "ARRIVED":
                return IN_TRANSIT;
            case "RECEIVED":
                return COMPLETED;
            case "CANCELLED":
                return CANCELLED;
            default:
                return null;
        }
    }
}
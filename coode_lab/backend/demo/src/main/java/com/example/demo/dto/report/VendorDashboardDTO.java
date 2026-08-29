package com.example.demo.dto.report;

// ========== Lombok ==========
import lombok.Getter;
import lombok.Setter;

// ========== Java ==========
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class VendorDashboardDTO {

    // ╔═══════╗
    // ║ Field ║
    // ╚═══════╝
    private Kpi revenue;
    private Kpi orderCount;
    private Kpi unitsSold;
    private Kpi returnQuantity;

    private List<DailyPoint> salesTrend;
    private List<StatusCount> salesStatus;

    private List<ProductRankItem> topProductsByQuantity;
    private List<ProductRankItem> topProductsByAmount;

    private ReturnSummary returnSummary;

    // ╔══════════════════════════════════════╗
    // ║ Nested class : KPI（含成長%）       ║
    // ╚══════════════════════════════════════╝
    // growthRate 為 null 表示無前期資料（前端顯示 -）
    @Getter
    @Setter
    public static class Kpi {
        private BigDecimal value;
        private BigDecimal growthRate;
    }

    // ╔══════════════════════════════════════╗
    // ║ Nested class : 銷售趨勢（逐日）     ║
    // ╚══════════════════════════════════════╝
    @Getter
    @Setter
    public static class DailyPoint {
        private LocalDate date;
        private BigDecimal amount;
    }

    // ╔══════════════════════════════════════╗
    // ║ Nested class : 銷售狀態（4 群組）   ║
    // ╚══════════════════════════════════════╝
    @Getter
    @Setter
    public static class StatusCount {
        private String status;
        private Long count;
    }

    // ╔══════════════════════════════════════╗
    // ║ Nested class : 商品排行             ║
    // ╚══════════════════════════════════════╝
    @Getter
    @Setter
    public static class ProductRankItem {
        private Long productId;
        private String name;
        private Long quantity;
        private BigDecimal amount;
    }

    // ╔══════════════════════════════════════╗
    // ║ Nested class : 退貨狀況             ║
    // ╚══════════════════════════════════════╝
    // appliedCount = pendingReview + approved + rejected
    @Getter
    @Setter
    public static class ReturnSummary {
        private Long appliedCount;
        private Long pendingReview;
        private Long approved;
        private Long rejected;
    }
}
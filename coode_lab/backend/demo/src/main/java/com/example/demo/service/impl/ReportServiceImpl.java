package com.example.demo.service.impl;

// ========== Spring ==========
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// ========== Java ==========
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// ========== Project ==========
import com.example.demo.dto.report.VendorDashboardDTO;
import com.example.demo.dto.report.VendorReportQuery;
import com.example.demo.repository.ReportRepository;
import com.example.demo.service.ReportService;
import com.example.demo.util.VendorStatusMapper;

@Service
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private static final String CANCELLED = "CANCELLED";

    // 同意 = 走過核准的所有狀態（含後續處理中狀態）
    private static final Set<String> APPROVED_STATUSES = Set.of(
            "APPROVED", "PROCESSING", "PROCESSED", "ARRIVED", "COMPLETED");

    // ╔═══════════════╗
    // ║ Constructor ║
    // ╚═══════════════╝
    private final ReportRepository reportRepository;

    public ReportServiceImpl(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @Override
    public VendorDashboardDTO getVendorDashboard(Long vendorId, VendorReportQuery query) {
        DateTimeWindow window = resolveWindow(query);
        int limit = query.getLimit() != null ? query.getLimit() : 5;

        VendorDashboardDTO dto = new VendorDashboardDTO();

        // ── KPI ──
        ReportRepository.SalesAggregate current = reportRepository.findSalesAggregate(vendorId, window.currentStart(), window.currentEnd());
        ReportRepository.SalesAggregate previous = reportRepository.findSalesAggregate(vendorId, window.previousStart(), window.previousEnd());

        dto.setRevenue(buildKpi(current.getRevenue(), previous.getRevenue()));
        dto.setOrderCount(buildKpi(toDecimal(current.getOrders()), toDecimal(previous.getOrders())));
        dto.setUnitsSold(buildKpi(toDecimal(current.getUnits()), toDecimal(previous.getUnits())));

        // ── 退貨（本期 + 前期都算，供成長%） ──
        ReturnStat currentReturn = aggregateReturns(reportRepository.countReturnsByStatus(vendorId, window.currentStart(), window.currentEnd()));
        ReturnStat previousReturn = aggregateReturns(reportRepository.countReturnsByStatus(vendorId, window.previousStart(), window.previousEnd()));

        dto.setReturnQuantity(buildKpi(currentReturn.approvedQuantity(), previousReturn.approvedQuantity()));
        dto.setReturnSummary(buildReturnSummary(currentReturn));

        // ── 趨勢 / 狀態 / 排行 ──
        dto.setSalesTrend(buildTrend(reportRepository.findDailySales(vendorId, window.currentStart(), window.currentEnd()), window));
        dto.setSalesStatus(buildSalesStatus(reportRepository.countByStatus(vendorId, window.currentStart(), window.currentEnd())));

        List<VendorDashboardDTO.ProductRankItem> products = reportRepository.findTopProducts(vendorId, window.currentStart(), window.currentEnd())
                .stream()
                .map(this::toProductRankItem)
                .toList();
        dto.setTopProductsByQuantity(products.stream()
                .sorted(Comparator.comparing(VendorDashboardDTO.ProductRankItem::getQuantity).reversed())
                .limit(limit)
                .toList());
        dto.setTopProductsByAmount(products.stream()
                .sorted(Comparator.comparing(VendorDashboardDTO.ProductRankItem::getAmount).reversed())
                .limit(limit)
                .toList());

        return dto;
    }

    // ╔══════════════════════════════════════╗
    // ║ 期間解析                             ║
    // ╚══════════════════════════════════════╝

    // 上半開區間 [start, end)：end 為「最後一天 + 1 天」
    private DateTimeWindow resolveWindow(VendorReportQuery query) {
        LocalDate base = query.getDate() != null ? query.getDate() : LocalDate.now();
        String period = query.getPeriod() != null ? query.getPeriod() : "MONTH";

        LocalDate currentStart;
        LocalDate currentEndExclusive;

        if (query.getStart() != null && query.getEnd() != null) {
            // 自訂制
            currentStart = query.getStart();
            currentEndExclusive = query.getEnd().plusDays(1);
        } else {
            switch (period) {
                case "DAY":
                    currentStart = base;
                    currentEndExclusive = base.plusDays(1);
                    break;
                case "WEEK":
                    currentStart = base.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                    currentEndExclusive = currentStart.plusWeeks(1);
                    break;
                case "QUARTER":
                    currentStart = startOfQuarter(base);
                    currentEndExclusive = currentStart.plusMonths(3);
                    break;
                case "YEAR":
                    currentStart = base.withDayOfYear(1);
                    currentEndExclusive = currentStart.plusYears(1);
                    break;
                case "MONTH":
                default:
                    currentStart = base.withDayOfMonth(1);
                    currentEndExclusive = currentStart.plusMonths(1);
                    break;
            }
        }

        long days = java.time.temporal.ChronoUnit.DAYS.between(currentStart, currentEndExclusive);
        LocalDate previousStart = currentStart.minusDays(days);
        LocalDate previousEndExclusive = currentStart;

        return new DateTimeWindow(
                currentStart.atStartOfDay(), currentEndExclusive.atStartOfDay(),
                previousStart.atStartOfDay(), previousEndExclusive.atStartOfDay());
    }

    private LocalDate startOfQuarter(LocalDate date) {
        int quarterStartMonth = ((date.getMonthValue() - 1) / 3) * 3 + 1;
        return LocalDate.of(date.getYear(), quarterStartMonth, 1);
    }

    // ╔══════════════════════════════════════╗
    // ║ KPI 與成長%                         ║
    // ╚══════════════════════════════════════╝

    private VendorDashboardDTO.Kpi buildKpi(BigDecimal current, BigDecimal previous) {
        VendorDashboardDTO.Kpi kpi = new VendorDashboardDTO.Kpi();
        kpi.setValue(current);
        kpi.setGrowthRate(growthRate(current, previous));
        return kpi;
    }

    private BigDecimal growthRate(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return null; // 無前期資料 -> 前端顯示 -
        }
        return current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 1, RoundingMode.HALF_UP);
    }

    private BigDecimal toDecimal(Long value) {
        return BigDecimal.valueOf(value != null ? value : 0L);
    }

    // ╔══════════════════════════════════════╗
    // ║ 趨勢 / 狀態 / 排行                 ║
    // ╚══════════════════════════════════════╝

    private List<VendorDashboardDTO.DailyPoint> buildTrend(List<ReportRepository.DailySales> rows, DateTimeWindow window) {
        Map<LocalDate, BigDecimal> byDate = new HashMap<>();
        for (ReportRepository.DailySales row : rows) {
            LocalDate date = LocalDate.of(row.getYr(), row.getMo(), row.getDy());
            byDate.put(date, row.getAmount());
        }

        List<VendorDashboardDTO.DailyPoint> trend = new ArrayList<>();
        for (LocalDate date = window.currentStart().toLocalDate();
             date.isBefore(window.currentEnd().toLocalDate());
             date = date.plusDays(1)) {
            VendorDashboardDTO.DailyPoint point = new VendorDashboardDTO.DailyPoint();
            point.setDate(date);
            point.setAmount(byDate.getOrDefault(date, BigDecimal.ZERO));
            trend.add(point);
        }
        return trend;
    }

    private List<VendorDashboardDTO.StatusCount> buildSalesStatus(List<ReportRepository.StatusCountRow> rows) {
        Map<String, Long> counts = new HashMap<>();
        for (ReportRepository.StatusCountRow row : rows) {
            counts.put(row.getStatus(), row.getCount());
        }

        List<VendorDashboardDTO.StatusCount> result = new ArrayList<>();
        for (String group : List.of(
                VendorStatusMapper.NOT_SHIPPED,
                VendorStatusMapper.IN_TRANSIT,
                VendorStatusMapper.COMPLETED,
                VendorStatusMapper.CANCELLED)) {
            VendorDashboardDTO.StatusCount item = new VendorDashboardDTO.StatusCount();
            item.setStatus(group);
            item.setCount(countsForGroup(counts, group));
            result.add(item);
        }
        return result;
    }

    private long countsForGroup(Map<String, Long> counts, String group) {
        long total = 0L;
        for (String rawStatus : VendorStatusMapper.toOrderItemStatuses(group)) {
            total += counts.getOrDefault(rawStatus, 0L);
        }
        return total;
    }

    private VendorDashboardDTO.ProductRankItem toProductRankItem(ReportRepository.ProductRank row) {
        VendorDashboardDTO.ProductRankItem item = new VendorDashboardDTO.ProductRankItem();
        item.setProductId(row.getProductId());
        item.setName(row.getProductName());
        item.setQuantity(row.getQuantity());
        item.setAmount(row.getAmount());
        return item;
    }

    // ╔══════════════════════════════════════╗
    // ║ 退貨                                 ║
    // ╚══════════════════════════════════════╝

    private ReturnStat aggregateReturns(List<ReportRepository.ReturnStatusRow> rows) {
        long pendingReview = 0L;
        long approvedCount = 0L;
        long rejectedCount = 0L;
        BigDecimal approvedQuantity = BigDecimal.ZERO;

        for (ReportRepository.ReturnStatusRow row : rows) {
            String status = row.getStatus();
            if ("PENDING_REVIEW".equals(status)) {
                pendingReview += row.getCount();
            } else if (APPROVED_STATUSES.contains(status)) {
                approvedCount += row.getCount();
                approvedQuantity = approvedQuantity.add(row.getReturnedQuantity());
            } else if ("REJECTED".equals(status)) {
                rejectedCount += row.getCount();
            } else if (CANCELLED.equals(status)) {
                // 取消的不計入任何群組
            }
        }

        long appliedCount = pendingReview + approvedCount + rejectedCount;
        return new ReturnStat(appliedCount, pendingReview, approvedCount, rejectedCount, approvedQuantity);
    }

    private VendorDashboardDTO.ReturnSummary buildReturnSummary(ReturnStat stat) {
        VendorDashboardDTO.ReturnSummary summary = new VendorDashboardDTO.ReturnSummary();
        summary.setAppliedCount(stat.appliedCount());
        summary.setPendingReview(stat.pendingReview());
        summary.setApproved(stat.approvedCount());
        summary.setRejected(stat.rejectedCount());
        return summary;
    }

    // ╔══════════════════════════════════════╗
    // ║ Internal record                     ║
    // ╚══════════════════════════════════════╝

    private record DateTimeWindow(LocalDateTime currentStart,
                                  LocalDateTime currentEnd,
                                  LocalDateTime previousStart,
                                  LocalDateTime previousEnd) {
    }

    private record ReturnStat(long appliedCount,
                              long pendingReview,
                              long approvedCount,
                              long rejectedCount,
                              BigDecimal approvedQuantity) {
    }
}
package com.example.demo.repository;

// ========== Spring ==========
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// ========== Java ==========
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// ========== Project ==========
import com.example.demo.model.OrderItem;

public interface ReportRepository extends JpaRepository<OrderItem, Long> {

    // ╔══════════════════════════════════════╗
    // ║ KPI 彙總（本期/前期共用同一查詢）   ║
    // ╚══════════════════════════════════════╝

    @Query(value = """
            select coalesce(sum(oi.priceTotal), 0)     as revenue,
                   coalesce(sum(oi.productQuantity), 0) as units,
                   count(distinct o.id)                 as orders
            from OrderItem oi
            join oi.order o
            where oi.vendor.vendorId = :vendorId
              and o.createdAt >= :start
              and o.createdAt < :end
              and oi.status <> 'CANCELLED'
            """)
    SalesAggregate findSalesAggregate(@Param("vendorId") Long vendorId,
                                      @Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end);

    // ╔══════════════════════════════════════╗
    // ║ 銷售趨勢（逐日金額）                ║
    // ╚══════════════════════════════════════╝

    @Query(value = """
            select year(o.createdAt) as yr,
                   month(o.createdAt) as mo,
                   day(o.createdAt) as dy,
                   coalesce(sum(oi.priceTotal), 0) as amount
            from OrderItem oi
            join oi.order o
            where oi.vendor.vendorId = :vendorId
              and o.createdAt >= :start
              and o.createdAt < :end
              and oi.status <> 'CANCELLED'
            group by year(o.createdAt), month(o.createdAt), day(o.createdAt)
            """)
    List<DailySales> findDailySales(@Param("vendorId") Long vendorId,
                                    @Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end);

    // ╔══════════════════════════════════════╗
    // ║ 銷售狀態（原始狀態分組）            ║
    // ╚══════════════════════════════════════╝

    @Query(value = """
            select oi.status as status, count(oi) as count
            from OrderItem oi
            join oi.order o
            where oi.vendor.vendorId = :vendorId
              and o.createdAt >= :start
              and o.createdAt < :end
            group by oi.status
            """)
    List<StatusCountRow> countByStatus(@Param("vendorId") Long vendorId,
                                       @Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);

    // ╔══════════════════════════════════════╗
    // ║ 商品排行（數量 + 金額一次取回）     ║
    // ╚══════════════════════════════════════╝

    @Query(value = """
            select oi.product.productId as productId,
                   oi.product.name as productName,
                   coalesce(sum(oi.productQuantity), 0) as quantity,
                   coalesce(sum(oi.priceTotal), 0) as amount
            from OrderItem oi
            join oi.order o
            where oi.vendor.vendorId = :vendorId
              and o.createdAt >= :start
              and o.createdAt < :end
              and oi.status <> 'CANCELLED'
            group by oi.product.productId, oi.product.name
            """)
    List<ProductRank> findTopProducts(@Param("vendorId") Long vendorId,
                                      @Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end);

    // ╔══════════════════════════════════════╗
    // ║ 退貨狀況（RETURN 限定，依狀態分組） ║
    // ╚══════════════════════════════════════╝

    @Query(value = """
            select ri.status as status,
                   count(ri) as count,
                   coalesce(sum(ri.approvalQuantity), 0) as approvalQuantity
            from ReturnItem ri
            join ri.returnRequest rr
            where rr.vendor.vendorId = :vendorId
              and rr.requestType = 'RETURN'
              and rr.createdAt >= :start
              and rr.createdAt < :end
            group by ri.status
            """)
    List<ReturnStatusRow> countReturnsByStatus(@Param("vendorId") Long vendorId,
                                               @Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end);

    // ╔══════════════════════════════════════╗
    // ║ Nested interface : 投影             ║
    // ╚══════════════════════════════════════╝

    interface SalesAggregate {
        BigDecimal getRevenue();

        Long getUnits();

        Long getOrders();
    }

    interface DailySales {
        Integer getYr();

        Integer getMo();

        Integer getDy();

        BigDecimal getAmount();
    }

    interface StatusCountRow {
        String getStatus();

        Long getCount();
    }

    interface ProductRank {
        Long getProductId();

        String getProductName();

        Long getQuantity();

        BigDecimal getAmount();
    }

    interface ReturnStatusRow {
        String getStatus();

        Long getCount();

        BigDecimal getReturnedQuantity();
    }
}
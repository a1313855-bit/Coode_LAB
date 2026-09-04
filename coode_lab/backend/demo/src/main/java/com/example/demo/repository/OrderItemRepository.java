package com.example.demo.repository;

// ========== Spring ==========
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;

// ========== Java ==========
import java.util.Collection;
import java.util.List;

// ========== Project ==========
import com.example.demo.model.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // 查詢某張訂單所有商品
    List<OrderItem> findByOrder_OrderId(Long orderId);

    // 廠商查看自己的訂單商品
    List<OrderItem> findByVendor_VendorId(Long vendorId);

    // 廠商查看自己的訂單商品（依狀態群組）
    List<OrderItem> findByVendor_VendorIdAndStatusIn(Long vendorId, Collection<String> statuses);

    // 熱銷商品：依營收（priceTotal）降序，取前 N 個商品的 productId
    @Query(value = "SELECT pv.product_id, SUM(oi.price_total) AS revenue " +
            "FROM order_items oi " +
            "JOIN product_variants pv ON oi.variant_id = pv.variant_id " +
            "JOIN products p ON pv.product_id = p.product_id " +
            "WHERE p.status = 'ACTIVE' " +
            "GROUP BY pv.product_id " +
            "ORDER BY SUM(oi.price_total) DESC",
            nativeQuery = true)
    List<Object[]> findTopSellingByRevenue(Pageable pageable);
}

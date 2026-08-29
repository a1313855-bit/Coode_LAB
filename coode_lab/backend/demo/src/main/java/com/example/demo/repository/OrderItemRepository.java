package com.example.demo.repository;

// ========== Spring ==========
import org.springframework.data.jpa.repository.JpaRepository;

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
}

package com.example.demo.repository;

import java.util.Optional;

// ========== Spring ==========
import org.springframework.data.jpa.repository.JpaRepository;

// ========== Project ==========
import com.example.demo.model.ReturnItem;

public interface ReturnItemRepository extends JpaRepository<ReturnItem, Long> {

    // 查詢一次申請內所有商品
    Optional<ReturnItem> findByReturnRequest_ReturnRequestsId(Long returnRequestId);
}

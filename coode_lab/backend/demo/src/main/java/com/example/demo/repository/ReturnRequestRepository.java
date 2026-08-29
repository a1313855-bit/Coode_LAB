package com.example.demo.repository;

// ========== Spring ==========
import org.springframework.data.jpa.repository.JpaRepository;

// ========== Java ==========
import java.util.List;

// ========== Project ==========
import com.example.demo.model.ReturnRequest;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {

    // 查詢會員所有申請
    List<ReturnRequest> findByUser_UserId(Long userId);

    // 查詢某廠商需要處理的申請
    List<ReturnRequest> findByVendor_VendorId(Long vendorId);
}

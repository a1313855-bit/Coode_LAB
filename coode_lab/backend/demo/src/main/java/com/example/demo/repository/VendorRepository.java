package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.example.demo.model.Vendor;

public interface VendorRepository extends JpaRepository<Vendor, Long> ,JpaSpecificationExecutor<Vendor>{

    // 根據email查詢廠商
    Optional<Vendor> findByEmail(String email);

    // 根據廠商名稱查詢（管理員新增測試商品使用固定廠商「測試」）
    Optional<Vendor> findByVendorName(String vendorName);

    boolean existsByEmail(String email);

    // 修改廠商時 Email 重複檢查
    boolean existsByEmailAndVendorIdNot(String email, Long vendorId);
}
package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    // 根據廠商ID查詢廠商的所有商品
    // 廠商後台可顯示我的產品
    List<Product> findByVendorVendorId(Long vendorId);

    //低庫存查詢
    List<Product> findByVendorVendorIdAndStockLessThanEqual(Long vendorId,Integer stock);
}
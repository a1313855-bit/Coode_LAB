package com.example.demo.service;

import java.math.BigDecimal;
import java.util.List;

import com.example.demo.dto.ProductRequest;
import com.example.demo.dto.ProductResponse;
import com.example.demo.dto.StockRequest;

public interface ProductService {

    // 查全部商品
    // 商城商品列表
    List<ProductResponse> findAll();

    // 查詢商城目前可販售、可顯示的商品
    List<ProductResponse> findAvailableProducts();

    // 根據productId查單一商品
    // 商品詳細頁
    ProductResponse findById(Long productId);

    // 根據廠商ID查詢廠商的所有商品
    List<ProductResponse> findByVendorId(Long vendorId);

    
    // 廠商新增商品
    ProductResponse createProduct(Long vendorId, ProductRequest request);

    // 上架按鈕（DRAFT → ACTIVE或INACTIVE → ACTIVE）
    ProductResponse activateProduct(Long vendorId, Long productId);
    
    // 批次上架
    List<ProductResponse> batchActivateProducts(Long vendorId,List<Long> productIds);

    // 修改商品
    // 廠商修改自己的商品
    ProductResponse updateProduct(Long vendorId, Long productId, ProductRequest request);

    // 修改商品庫存
    // 補貨或調整庫存
    // 只能修自己的商品
    ProductResponse updateStock(Long vendorId, Long productId, StockRequest request);

    //低庫存管理
    List<ProductResponse>findLowStockProducts(Long vendorId);

    // 下架商品
    // 廠商下架自己的商品
    ProductResponse deactivateProduct(Long vendorId, Long productId);

    //批次下架
    List<ProductResponse> batchDeactivateProducts(Long vendorId,List<Long> productIds);

    // 商城多條件搜尋商品
    List<ProductResponse> searchProducts(
            String keyword,
            String categoryType,
            String style,
            String color,
            String size,
            String pattern,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Long vendorId);

    // 管理員使用,多條件搜尋商品
    List<ProductResponse> adminSearchProducts(
            String keyword,
            String categoryType,
            String style,
            String color,
            String size,
            String pattern,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String status,
            Long vendorId);

    // 廠商自己的商品搜尋
    List<ProductResponse> vendorSearchProducts(
            Long vendorId,
            String keyword,
            String categoryType,
            String style,
            String color,
            String size,
            String pattern,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String status);

}
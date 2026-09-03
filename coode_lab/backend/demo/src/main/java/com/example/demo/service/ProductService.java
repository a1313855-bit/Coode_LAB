package com.example.demo.service;

import java.math.BigDecimal;
import java.util.List;

import com.example.demo.dto.ProductRequest;
import com.example.demo.dto.ProductResponse;
import com.example.demo.dto.ProductVariantResponse;
import com.example.demo.dto.ReplenishRequest;
import com.example.demo.dto.StockRequest;
import com.example.demo.util.SelectPartOfData;

public interface ProductService {

    // 查全部商品 (固定每頁10筆,page 從 0 開始)
    SelectPartOfData.Result<ProductResponse> findAll(int page);

    // 查詢商城目前可販售、可顯示的商品 (固定每頁10筆,page 從 0 開始)
    SelectPartOfData.Result<ProductResponse> findAvailableProducts(int page);

    // 根據productId查單一商品
    // 商品詳細頁
    ProductResponse findById(Long productId);

    // 根據廠商ID查詢廠商的所有商品 (固定每頁10筆,page 從 0 開始)
    SelectPartOfData.Result<ProductResponse> findByVendorId(Long vendorId, int page);

    // 廠商新增商品（含全部規格）
    ProductResponse createProduct(Long vendorId, ProductRequest request);

    // 上架按鈕（DRAFT → ACTIVE 或 INACTIVE → ACTIVE）
    ProductResponse activateProduct(Long vendorId, Long productId);

    ProductResponse setToDraftProduct(Long vendorId, Long productId);

    // 批次上架
    List<ProductResponse> batchActivateProducts(Long vendorId, List<Long> productIds);

    // 修改商品（含重新設定全部規格）
    ProductResponse updateProduct(Long vendorId, Long productId, ProductRequest request);

    // 修改單一規格庫存
    // 補貨或調整庫存
    // 只能修自己商品的規格
    ProductVariantResponse updateVariantStock(Long vendorId, Long variantId, StockRequest request);

    // 補貨：原庫存 + 本次補貨數量（不可為 0 / 負數，只能補自己的規格）
    ProductVariantResponse replenishVariantStock(Long vendorId, Long variantId, ReplenishRequest request);

    // 修改單一規格販售狀態（停售/恢復單一規格）
    ProductVariantResponse updateVariantStatus(Long vendorId, Long variantId, String status);

    // 整批停售/恢復：依顏色 或 依尺寸 一次性更新某商品下的規格
    List<ProductVariantResponse> batchUpdateVariantStatus(Long vendorId, Long productId, String color, String size, String status);

    // 低庫存管理（商品底下有任一規格庫存 ≤5 即納入）(固定每頁10筆,page 從 0 開始)
    SelectPartOfData.Result<ProductResponse> findLowStockProducts(Long vendorId, int page);

    // 下架商品
    // 廠商下架自己的商品
    ProductResponse deactivateProduct(Long vendorId, Long productId);

    // 批次下架
    List<ProductResponse> batchDeactivateProducts(Long vendorId, List<Long> productIds);

    // 商城多條件搜尋商品 (固定每頁10筆,page 從 0 開始)
    SelectPartOfData.Result<ProductResponse> searchProducts(
            int page,
            String keyword,
            String categoryType,
            String style,
            String color,
            String size,
            String pattern,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Long vendorId);

    // 管理員使用,多條件搜尋商品 (固定每頁10筆,page 從 0 開始)
    SelectPartOfData.Result<ProductResponse> adminSearchProducts(
            int page,
            String keyword,
            String categoryType,
            String style,
            String color,
            String size,
            String pattern,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String status,
            String vendorName);

    // 管理員修改商品基本資料（不動規格）
    ProductResponse adminUpdateProduct(Long productId, ProductRequest request);

    // 廠商自己的商品搜尋 (固定每頁10筆,page 從 0 開始)
    SelectPartOfData.Result<ProductResponse> vendorSearchProducts(
            int page,
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
package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.ProductVariant;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    // 某商品的全部規格
    List<ProductVariant> findByProduct_ProductId(Long productId);

    // 某廠商的全部規格（用於低庫存、變體管理）
    List<ProductVariant> findByProduct_Vendor_VendorId(Long vendorId);

    // 某廠商的低庫存規格
    List<ProductVariant> findByProduct_Vendor_VendorIdAndStockLessThanEqual(Long vendorId, Integer stock);

    // 某商品之下、指定顏色的所有規格（整批停售/恢復某顏色）
    List<ProductVariant> findByProduct_ProductIdAndColor(Long productId, String color);

    // 某商品之下、指定尺寸的所有規格（整批停售/恢復某尺寸）
    List<ProductVariant> findByProduct_ProductIdAndSize(Long productId, String size);

    // 某商品之下、指定顏色＋尺寸的規格（單一規格）
    List<ProductVariant> findByProduct_ProductIdAndColorAndSize(Long productId, String color, String size);
}
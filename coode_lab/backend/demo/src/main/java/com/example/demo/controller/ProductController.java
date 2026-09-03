package com.example.demo.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.BatchProductRequest;
import com.example.demo.dto.ProductRequest;
import com.example.demo.dto.ProductResponse;
import com.example.demo.dto.ProductVariantResponse;
import com.example.demo.dto.ReplenishRequest;
import com.example.demo.dto.StockRequest;
import com.example.demo.dto.VariantBatchStatusRequest;
import com.example.demo.dto.VariantStatusRequest;
import com.example.demo.service.ProductService;
import com.example.demo.util.SelectPartOfData;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/coode_lab/products")
public class ProductController {

    private final ProductService productService;

    // 建構子注入
    // Spring Boot 會自動把 ProductService 放進來
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // 查全部商品,管理員用 (固定每頁10筆,page 從 0 開始)
    @GetMapping
    public SelectPartOfData.Result<ProductResponse> findAll(
            @RequestParam(defaultValue = "0") int page) {
        return productService.findAll(page);
    }

    // 查詢商城目前可以顯示的商品,商城使用 (固定每頁10筆,page 從 0 開始)
    // 只有 Product = ACTIVE
    // 且 Vendor = ACTIVE
    // 且 Vendor 合約尚未到期才會出現
    @GetMapping("/available")
    public SelectPartOfData.Result<ProductResponse> findAvailableProducts(
            @RequestParam(defaultValue = "0") int page) {

        return productService.findAvailableProducts(page);
    }

    // 管理員/廠商後台查詢商品用
    @GetMapping("/{productId}")
    public ProductResponse findById(@PathVariable Long productId) {
        return productService.findById(productId);
    }

    // 管理員/廠商後台,看某廠商的全部商品用 (固定每頁10筆,page 從 0 開始)
    @GetMapping("/vendor/{vendorId}")
    public SelectPartOfData.Result<ProductResponse> findByVendorId(
            @PathVariable Long vendorId,
            @RequestParam(defaultValue = "0") int page) {
        return productService.findByVendorId(vendorId, page);
    }

    // 商城多條件搜尋商品 (固定每頁10筆,page 從 0 開始)
    @GetMapping("/filter")
    public SelectPartOfData.Result<ProductResponse> searchProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categoryType,
            @RequestParam(required = false) String style,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String size,
            @RequestParam(required = false) String pattern,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Long vendorId) {

        return productService.searchProducts(
                page,
                keyword,
                categoryType,
                style,
                color,
                size,
                pattern,
                minPrice,
                maxPrice,
                vendorId);
    }

    // 管理員後台多條件搜尋商品 (固定每頁10筆,page 從 0 開始)
    @GetMapping("/admin/filter")
    public SelectPartOfData.Result<ProductResponse> adminSearchProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categoryType,
            @RequestParam(required = false) String style,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String size,
            @RequestParam(required = false) String pattern,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String vendorName) {

        return productService.adminSearchProducts(
                page,
                keyword,
                categoryType,
                style,
                color,
                size,
                pattern,
                minPrice,
                maxPrice,
                status,
                vendorName);
    }

    // 管理員修改商品基本資料（不動規格）
    @PutMapping("/admin/{productId}")
    public ProductResponse adminUpdateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductRequest request) {
        return productService.adminUpdateProduct(productId, request);
    }

    // 廠商後台搜尋自己的商品 (固定每頁10筆,page 從 0 開始)
    @GetMapping("/vendor/filter")
    public SelectPartOfData.Result<ProductResponse> vendorSearchProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam Long vendorId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categoryType,
            @RequestParam(required = false) String style,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String size,
            @RequestParam(required = false) String pattern,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String status) {

        return productService.vendorSearchProducts(
                page,
                vendorId,
                keyword,
                categoryType,
                style,
                color,
                size,
                pattern,
                minPrice,
                maxPrice,
                status);
    }

    // 新增商品
    @PostMapping("/vendor/{vendorId}")
    public ProductResponse createProduct(@PathVariable Long vendorId, @Valid @RequestBody ProductRequest request) {
        return productService.createProduct(vendorId, request);

    }

    // 修改商品
    @PutMapping("/vendor/{vendorId}/{productId}")
    public ProductResponse updateProduct(@PathVariable Long vendorId, @PathVariable Long productId,
            @Valid @RequestBody ProductRequest request) {
        return productService.updateProduct(vendorId, productId, request);
    }

    // 廠商修改自己商品的單一規格庫存
    @PatchMapping("/vendor/{vendorId}/variants/{variantId}/stock")
    public ProductVariantResponse updateVariantStock(@PathVariable Long vendorId, @PathVariable Long variantId,
            @Valid @RequestBody StockRequest request) {
        return productService.updateVariantStock(vendorId, variantId, request);
    }

    // 廠商低庫存補貨：原庫存 + 本次數量
    @PatchMapping("/vendor/{vendorId}/variants/{variantId}/replenish")
    public ProductVariantResponse replenishVariantStock(@PathVariable Long vendorId, @PathVariable Long variantId,
            @Valid @RequestBody ReplenishRequest request) {
        return productService.replenishVariantStock(vendorId, variantId, request);
    }

    // 廠商停售/恢復單一規格
    @PatchMapping("/vendor/{vendorId}/variants/{variantId}/status")
    public ProductVariantResponse updateVariantStatus(@PathVariable Long vendorId, @PathVariable Long variantId,
            @Valid @RequestBody VariantStatusRequest request) {
        return productService.updateVariantStatus(vendorId, variantId, request.getStatus());
    }

    // 廠商整批停售/恢復某商品的「某顏色」或「某尺寸」
    @PatchMapping("/vendor/{vendorId}/products/{productId}/variants/batch-status")
    public List<ProductVariantResponse> batchUpdateVariantStatus(@PathVariable Long vendorId,
            @PathVariable Long productId,
            @Valid @RequestBody VariantBatchStatusRequest request) {
        return productService.batchUpdateVariantStatus(vendorId, productId,
                request.getColor(), request.getSize(), request.getStatus());
    }

    //低庫存管理 (固定每頁10筆,page 從 0 開始)
    @GetMapping("/vendor/{vendorId}/low-stock")
    public SelectPartOfData.Result<ProductResponse> findLowStockProducts(
            @PathVariable Long vendorId,
            @RequestParam(defaultValue = "0") int page){
        return productService.findLowStockProducts(vendorId, page);
    }

    // 廠商上架自己商品按鈕
    @PatchMapping("/vendor/{vendorId}/{productId}/activate")
    public ProductResponse activateProduct(@PathVariable Long vendorId, @PathVariable Long productId) {
        return productService.activateProduct(vendorId, productId);
    }

    //廠商批次上架
    @PatchMapping("/vendor/{vendorId}/batch-activate")
    public List<ProductResponse> batchActivateProducts(@PathVariable Long vendorId,@Valid @RequestBody BatchProductRequest request){
        return productService.batchActivateProducts(vendorId, request.getProductIds());
    }

    // 廠商下架自己商品按鈕
    @PatchMapping("/vendor/{vendorId}/{productId}/deactivate")
    public ProductResponse deactivateProduct(@PathVariable Long vendorId, @PathVariable Long productId) {
        return productService.deactivateProduct(vendorId, productId);
    }

    // 廠商將下架商品改為待上架
    @PatchMapping("/vendor/{vendorId}/{productId}/draft")
    public ProductResponse setToDraftProduct(@PathVariable Long vendorId, @PathVariable Long productId) {
        return productService.setToDraftProduct(vendorId, productId);
    }

    //廠商批次下架
    @PatchMapping("/vendor/{vendorId}/batch-deactivate")
    public List<ProductResponse> batchDeactivateProducts(@PathVariable Long vendorId,@Valid @RequestBody BatchProductRequest request){
        return productService.batchDeactivateProducts(vendorId, request.getProductIds());
    }

}

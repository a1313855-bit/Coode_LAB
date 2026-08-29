package com.example.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.BatchProductRequest;
import com.example.demo.dto.ProductRequest;
import com.example.demo.dto.ProductResponse;
import com.example.demo.dto.StockRequest;
import com.example.demo.service.ProductService;

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

    // 查全部商品,管理員用
    @GetMapping
    public List<ProductResponse> findAll() {
        return productService.findAll();
    }

    // 查詢商城目前可以顯示的商品,商城使用
    // 只有 Product = ACTIVE
    // 且 Vendor = ACTIVE
    // 且 Vendor 合約尚未到期才會出現
    @GetMapping("/available")
    public List<ProductResponse> findAvailableProducts() {

        return productService.findAvailableProducts();
    }

    // 管理員/廠商後台查詢商品用
    @GetMapping("/{productId}")
    public ProductResponse findById(@PathVariable Long productId) {
        return productService.findById(productId);
    }

    // 管理員/廠商後台,看某廠商的全部商品用
    @GetMapping("/vendor/{vendorId}")
    public List<ProductResponse> findByVendorId(@PathVariable Long vendorId) {
        return productService.findByVendorId(vendorId);
    }

    // 商城多條件搜尋商品
    @GetMapping("/filter")
    public List<ProductResponse> searchProducts(
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

    // 管理員後台多條件搜尋商品
    @GetMapping("/admin/filter")
    public List<ProductResponse> adminSearchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categoryType,
            @RequestParam(required = false) String style,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String size,
            @RequestParam(required = false) String pattern,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long vendorId) {

        return productService.adminSearchProducts(
                keyword,
                categoryType,
                style,
                color,
                size,
                pattern,
                minPrice,
                maxPrice,
                status,
                vendorId);
    }

    // 廠商後台搜尋自己的商品
    @GetMapping("/vendor/filter")
    public List<ProductResponse> vendorSearchProducts(
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
    public ProductResponse createProduct(@PathVariable Long vendorId, @RequestBody ProductRequest request) {
        return productService.createProduct(vendorId, request);

    }

    // 修改商品
    @PutMapping("/vendor/{vendorId}/{productId}")
    public ProductResponse updateProduct(@PathVariable Long vendorId, @PathVariable Long productId,
            @RequestBody ProductRequest request) {
        return productService.updateProduct(vendorId, productId, request);
    }

    // 廠商修改自己商品庫存
    @PatchMapping("/vendor/{vendorId}/{productId}/stock")
    public ProductResponse updateStock(@PathVariable Long vendorId, @PathVariable Long productId,
            @RequestBody StockRequest request) {
        return productService.updateStock(vendorId, productId, request);
    }

    //低庫存管理
    @GetMapping("/vendor/{vendorId}/low-stock")
    public List<ProductResponse> findLowStockProducts(@PathVariable Long vendorId){
        return productService.findLowStockProducts(vendorId);
    }

    // 廠商上架自己商品按鈕
    @PatchMapping("/vendor/{vendorId}/{productId}/activate")
    public ProductResponse activateProduct(@PathVariable Long vendorId, @PathVariable Long productId) {
        return productService.activateProduct(vendorId, productId);
    }

    //廠商批次上架
    @PatchMapping("/vendor/{vendorId}/batch-activate")
    public List<ProductResponse> batchActivateProducts(@PathVariable Long vendorId,@RequestBody BatchProductRequest request){
        return productService.batchActivateProducts(vendorId, request.getProductIds());
    }

    // 廠商下架自己商品按鈕
    @PatchMapping("/vendor/{vendorId}/{productId}/deactivate")
    public ProductResponse deactivateProduct(@PathVariable Long vendorId, @PathVariable Long productId) {
        return productService.deactivateProduct(vendorId, productId);
    }

    //廠商批次下架
    @PatchMapping("/vendor/{vendorId}/batch-deactivate")
    public List<ProductResponse> batchDeactivateProducts(@PathVariable Long vendorId,@RequestBody BatchProductRequest request){
        return productService.batchDeactivateProducts(vendorId, request.getProductIds());
    }

}

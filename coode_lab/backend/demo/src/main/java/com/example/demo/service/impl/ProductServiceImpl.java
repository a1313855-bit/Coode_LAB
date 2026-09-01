package com.example.demo.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.demo.dto.ProductRequest;
import com.example.demo.dto.ProductResponse;
import com.example.demo.dto.StockRequest;
import com.example.demo.exception.ProductSpecification;
import com.example.demo.model.Product;
import com.example.demo.model.Vendor;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.VendorRepository;
import com.example.demo.service.ProductService;
import com.example.demo.util.SelectPartOfData;

@Service

public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final VendorRepository vendorRepository;

    // 建構子注入
    public ProductServiceImpl(ProductRepository productRepository, VendorRepository vendorRepository) {
        this.productRepository = productRepository;
        this.vendorRepository = vendorRepository;
    }

    // 查所有商品 (固定每頁10筆)
    @Override
    public SelectPartOfData.Result<ProductResponse> findAll(int page) {
        List<ProductResponse> all = productRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
        return SelectPartOfData.pageOf10(all, page);
    }

    // 商城呼叫使用,只顯示已上架且廠商狀態是啟用的商品 (固定每頁10筆)
    @Override
    public SelectPartOfData.Result<ProductResponse> findAvailableProducts(int page) {

        Specification<Product> spec = ProductSpecification.isActive()
                .and(ProductSpecification.vendorIsActive())
                .and(ProductSpecification.vendorContractNotExpired());

        List<ProductResponse> all = productRepository
                .findAll(spec)
                .stream()
                .map(this::toResponse)
                .toList();
        return SelectPartOfData.pageOf10(all, page);
    }

    // 根據productId查詢單一商品
    @Override
    public ProductResponse findById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("找不到商品 ID:" + productId));
        return toResponse(product);

    }

    // 根據廠商ID查詢所有商品 (固定每頁10筆)
    @Override
    public SelectPartOfData.Result<ProductResponse> findByVendorId(Long vendorId, int page) {
        List<ProductResponse> all = productRepository.findByVendorVendorId(vendorId)
                .stream()
                .map(this::toResponse)
                .toList();
        return SelectPartOfData.pageOf10(all, page);
    }

    // 商城搜尋-多條件搜尋商品 (固定每頁10筆)
    @Override
    public SelectPartOfData.Result<ProductResponse> searchProducts(
            int page,
            String keyword,
            String categoryType,
            String style,
            String color,
            String size,
            String pattern,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Long vendorId) {

        // 1. 先建立搜尋條件
        // 商品必須 ACTIVE，而且 Vendor 也必須 ACTIVE,廠商合約到期時間需大於等於現在時間
        Specification<Product> spec = ProductSpecification.isActive()
                .and(ProductSpecification.vendorIsActive())
                .and(ProductSpecification.vendorContractNotExpired())

                // 2. 以下條件有傳值才會真的限制
                .and(ProductSpecification.nameContains(keyword))
                .and(ProductSpecification.hasCategoryType(categoryType))
                .and(ProductSpecification.hasStyle(style))
                .and(ProductSpecification.hasColor(color))
                .and(ProductSpecification.hasSize(size))
                .and(ProductSpecification.hasPattern(pattern))
                .and(ProductSpecification.priceGreaterThanOrEqual(minPrice))
                .and(ProductSpecification.priceLessThanOrEqual(maxPrice))
                .and(ProductSpecification.hasVendorId(vendorId));

        // 3. Repository 根據組好的條件查商品
        List<ProductResponse> all = productRepository.findAll(spec)
                .stream()

                // 4. Product Entity → ProductResponse DTO
                .map(this::toResponse)

                // 5. 重新組成 List
                .toList();
        return SelectPartOfData.pageOf10(all, page);
    }

    // 管理員後台多條件搜尋 (固定每頁10筆)
    @Override
    public SelectPartOfData.Result<ProductResponse> adminSearchProducts(
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
            Long vendorId) {

        Specification<Product> spec = ProductSpecification.nameContains(keyword)
                .and(ProductSpecification.hasCategoryType(categoryType))
                .and(ProductSpecification.hasStyle(style))
                .and(ProductSpecification.hasColor(color))
                .and(ProductSpecification.hasSize(size))
                .and(ProductSpecification.hasPattern(pattern))
                .and(ProductSpecification.priceGreaterThanOrEqual(minPrice))
                .and(ProductSpecification.priceLessThanOrEqual(maxPrice))
                .and(ProductSpecification.hasStatus(status))
                .and(ProductSpecification.hasVendorId(vendorId));

        List<ProductResponse> all = productRepository.findAll(spec)
                .stream()
                .map(this::toResponse)
                .toList();
        return SelectPartOfData.pageOf10(all, page);
    }

    // 廠商後台多條件搜尋商品 (固定每頁10筆)
    @Override
    public SelectPartOfData.Result<ProductResponse> vendorSearchProducts(
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
            String status) {

        Specification<Product> spec = ProductSpecification.hasVendorId(vendorId)
                .and(ProductSpecification.nameContains(keyword))
                .and(ProductSpecification.hasCategoryType(categoryType))
                .and(ProductSpecification.hasStyle(style))
                .and(ProductSpecification.hasColor(color))
                .and(ProductSpecification.hasSize(size))
                .and(ProductSpecification.hasPattern(pattern))
                .and(ProductSpecification.priceGreaterThanOrEqual(minPrice))
                .and(ProductSpecification.priceLessThanOrEqual(maxPrice))
                .and(ProductSpecification.hasStatus(status));

        List<ProductResponse> all = productRepository.findAll(spec)
                .stream()
                .map(this::toResponse)
                .toList();
        return SelectPartOfData.pageOf10(all, page);
    }

    // 廠商新增商品
    @Override
    public ProductResponse createProduct(Long vendorId, ProductRequest request) {

        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("找不到廠商 ID:" + vendorId));
        if (!"ACTIVE".equals(vendor.getStatus())) {
            throw new RuntimeException("廠商帳號尚未啟用或已停權");
        }

        if (vendor.getContractExpiresAt() == null) {
            throw new RuntimeException("廠商尚未設定合約到期日");
        }

        if (!vendor.getContractExpiresAt().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("廠商合約已到期");
        }

        if (request.getName() == null || request.getName().isBlank()) {
            throw new RuntimeException("商品名稱不能為空");
        }

        if (request.getColor() == null || request.getColor().isBlank()) {
            throw new RuntimeException("商品顏色不能為空");
        }

        if (request.getSize() == null || request.getSize().isBlank()) {
            throw new RuntimeException("商品尺寸不能為空");
        }

        if (request.getStock() == null || request.getStock() < 0) {
            throw new RuntimeException("庫存數量不能為空或小於0");
        }

        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("商品價格不能為空或小於0");
        }

        // 建立真正要存進資料庫的 Product Entity
        Product product = new Product();

        // 把 Request DTO 的資料放進 Product
        product.setName(request.getName());
        product.setPattern(request.getPattern());
        product.setCategoryType(request.getCategoryType());
        product.setStyle(request.getStyle());
        product.setColor(request.getColor());
        product.setSize(request.getSize());
        product.setStock(request.getStock());
        product.setPrice(request.getPrice());
        product.setDescription(request.getDescription());
        product.setImagesJpg(request.getImagesJpg());
        product.setOutfitPng(request.getOutfitPng());

        // 取得商品狀態
        String status = request.getStatus();

        // 只允許直接上架或待上架
        if (!"ACTIVE".equals(status) && !"DRAFT".equals(status)) {
            throw new RuntimeException("商品狀態只能選擇直接上架或待上架");
        }

        product.setStatus(status);

        // 設定這個商品屬於哪個 Vendor
        product.setVendor(vendor);

        // 存進資料庫
        Product savedProduct = productRepository.save(product);

        // Entity → Response DTO，回傳給前端
        return toResponse(savedProduct);

    }

    // 廠商上架自己產品,上架按鈕
    @Override
    public ProductResponse activateProduct(Long vendorId, Long productId) {
        // 找到商品
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("找不到商品 ID：" + productId));

        // 確認商品是不是這個廠商的
        if (!product.getVendor().getVendorId().equals(vendorId)) {
            throw new RuntimeException("你沒有權限上架其他廠商的商品");
        }

        // 取得這個商品所屬的廠商
        Vendor vendor = product.getVendor();

        // 檢查廠商帳號是否正常
        if (!"ACTIVE".equals(vendor.getStatus())) {
            throw new RuntimeException("廠商帳號尚未啟用或已停權，無法上架商品");
        }

        // 檢查廠商是否有設定合約
        if (vendor.getContractExpiresAt() == null) {
            throw new RuntimeException("廠商尚未設定合約到期日，無法上架商品");
        }

        // 檢查廠商合約是否已到期
        if (!vendor.getContractExpiresAt().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("廠商合約已到期，無法上架商品");
        }

        // 商品改成上架狀態
        product.setStatus("ACTIVE");

        // 儲存
        Product savedProduct = productRepository.save(product);

        // Product Entity → ProductResponse DTO
        return toResponse(savedProduct);
    }

    // 批次上架
    @Override
    public List<ProductResponse> batchActivateProducts(Long vendorId, List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            throw new RuntimeException("請至少選擇一個要上架的商品");
        }

        List<ProductResponse> result = new ArrayList<>();

        for (Long productId : productIds) {
            ProductResponse product = activateProduct(vendorId, productId);

            result.add(product);
        }
        return result;
    }

    // 廠商下架自己的商品,下架按鈕
    @Override
    public ProductResponse deactivateProduct(Long vendorId, Long productId) {

        // 找到要下架的商品
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("找不到商品 ID：" + productId));

        // 確認這個商品是不是這個廠商的
        if (!product.getVendor().getVendorId().equals(vendorId)) {
            throw new RuntimeException("你沒有權限下架其他廠商的商品");
        }

        // 把商品狀態改成 INACTIVE
        product.setStatus("INACTIVE");

        // 儲存修改後的商品
        Product savedProduct = productRepository.save(product);

        // Entity 轉成 DTO 回傳
        return toResponse(savedProduct);
    }

    // 批次下架
    @Override
    public List<ProductResponse> batchDeactivateProducts(
            Long vendorId,
            List<Long> productIds) {

        List<ProductResponse> result = new ArrayList<>();

        for (Long productId : productIds) {

            ProductResponse product = deactivateProduct(vendorId, productId);

            result.add(product);
        }

        return result;
    }

    // 廠商修改自己的商品
    @Override
    public ProductResponse updateProduct(Long vendorId, Long productId, ProductRequest request) {

        // 找尋要修改的商品
        Product oldProduct = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("找不到商品 ID:" + productId));

        // 確認這個商品是不是這個廠商的
        // 拿商品原本的 Vendor ID 跟現在傳進來的 vendorId 比較。
        if (!oldProduct.getVendor().getVendorId().equals(vendorId)) {
            throw new RuntimeException("你沒有權限修改其他廠商的商品");
        }

        if (request.getName() == null || request.getName().isBlank()) {
            throw new RuntimeException("商品名稱不能為空");
        }

        if (request.getColor() == null || request.getColor().isBlank()) {
            throw new RuntimeException("商品顏色不能為空");
        }

        if (request.getSize() == null || request.getSize().isBlank()) {
            throw new RuntimeException("商品尺寸不能為空");
        }

        if (request.getStock() == null || request.getStock() < 0) {
            throw new RuntimeException("庫存數量不能為空或小於0");
        }

        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("商品價格不能為空或小於0");
        }

        // 把前端傳來的新資料放進舊商品
        oldProduct.setName(request.getName());
        oldProduct.setPattern(request.getPattern());
        oldProduct.setCategoryType(request.getCategoryType());
        oldProduct.setStyle(request.getStyle());
        oldProduct.setColor(request.getColor());
        oldProduct.setSize(request.getSize());
        oldProduct.setStock(request.getStock());
        oldProduct.setPrice(request.getPrice());
        oldProduct.setDescription(request.getDescription());
        oldProduct.setImagesJpg(request.getImagesJpg());
        oldProduct.setOutfitPng(request.getOutfitPng());

        // 更新資料庫
        Product savedProduct = productRepository.save(oldProduct);

        // Entity → DTO
        return toResponse(savedProduct);

    }

    // 修改商品庫存
    @Override
    public ProductResponse updateStock(Long vendorId, Long productId, StockRequest request) {

        // 找尋要修改的商品
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("找不到商品 ID:" + productId));

        // 確認這個商品是不是這個廠商的
        if (!product.getVendor().getVendorId().equals(vendorId)) {
            throw new RuntimeException("你沒有權限修改其他廠商的商品");
        }

        // 從 StockRequest 裡面取得前端傳來的新庫存
        Integer stock = request.getStock();

        // 檢查庫存數量是否為空或小於0
        if (stock == null || stock < 0) {
            throw new RuntimeException("庫存數量不能為空或小於0");
        }

        // 修改 Product Entity 的庫存
        product.setStock(stock);

        // 儲存進資料庫
        Product savedProduct = productRepository.save(product);

        // Product Entity 轉成 ProductResponse DTO
        return toResponse(savedProduct);

    }

    // 低庫存查詢 (固定每頁10筆)
    @Override
    public SelectPartOfData.Result<ProductResponse> findLowStockProducts(Long vendorId, int page) {
        List<Product> products = productRepository.findByVendorVendorIdAndStockLessThanEqual(vendorId, 10);

        List<ProductResponse> all = products
                .stream()
                .map(this::toResponse)
                .toList();
        return SelectPartOfData.pageOf10(all, page);
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getProductId(),
                product.getName(),
                product.getPattern(),
                product.getCategoryType(),
                product.getStyle(),
                product.getColor(),
                product.getSize(),
                product.getStock(),
                product.getPrice(),
                product.getDescription(),
                product.getImagesJpg(),
                product.getOutfitPng(),
                product.getStatus(),
                product.getVendor().getVendorId(),
                product.getVendor().getVendorName()

        );
    }

}

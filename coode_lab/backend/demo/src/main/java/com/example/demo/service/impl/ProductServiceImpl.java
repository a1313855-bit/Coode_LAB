package com.example.demo.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.ProductRequest;
import com.example.demo.dto.ProductResponse;
import com.example.demo.dto.ProductVariantRequest;
import com.example.demo.dto.ProductVariantResponse;
import com.example.demo.dto.ReplenishRequest;
import com.example.demo.dto.StockRequest;
import com.example.demo.exception.ProductSpecification;
import com.example.demo.model.Product;
import com.example.demo.model.ProductVariant;
import com.example.demo.model.Vendor;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.ProductVariantRepository;
import com.example.demo.repository.VendorRepository;
import com.example.demo.service.ProductService;
import com.example.demo.util.SelectPartOfData;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final VendorRepository vendorRepository;
    private final ProductVariantRepository productVariantRepository;

    // 建構子注入
    public ProductServiceImpl(ProductRepository productRepository,
            VendorRepository vendorRepository,
            ProductVariantRepository productVariantRepository) {
        this.productRepository = productRepository;
        this.vendorRepository = vendorRepository;
        this.productVariantRepository = productVariantRepository;
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
            String vendorName) {

        Specification<Product> spec = ProductSpecification.nameContains(keyword)
                .and(ProductSpecification.hasCategoryType(categoryType))
                .and(ProductSpecification.hasStyle(style))
                .and(ProductSpecification.hasColor(color))
                .and(ProductSpecification.hasSize(size))
                .and(ProductSpecification.hasPattern(pattern))
                .and(ProductSpecification.priceGreaterThanOrEqual(minPrice))
                .and(ProductSpecification.priceLessThanOrEqual(maxPrice))
                .and(ProductSpecification.hasStatus(status))
                .and(ProductSpecification.hasVendorName(vendorName));

        List<ProductResponse> all = productRepository.findAll(spec)
                .stream()
                .map(this::toResponse)
                .toList();
        return SelectPartOfData.pageOf10(all, page);
    }

    // 管理員修改商品基本資料（不動規格）
    @Override
    @Transactional
    public ProductResponse adminUpdateProduct(Long productId, ProductRequest request) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("找不到商品 ID:" + productId));

        if (request.getName() != null && !request.getName().isBlank()) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null && request.getPrice().compareTo(BigDecimal.ZERO) >= 0) {
            product.setPrice(request.getPrice());
        }
        if (request.getCategoryType() != null && !request.getCategoryType().isBlank()) {
            product.setCategoryType(request.getCategoryType());
        }
        if (request.getStyle() != null) {
            product.setStyle(request.getStyle());
        }
        if (request.getPattern() != null) {
            product.setPattern(request.getPattern());
        }

        String status = request.getStatus();
        if (status != null && ("ACTIVE".equals(status) || "INACTIVE".equals(status) || "DRAFT".equals(status))) {
            product.setStatus(status);
        }

        Product saved = productRepository.save(product);
        return toResponse(saved);
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

    // ╔══════════════════════════════════════╗
    // ║ 廠商新增商品（含全部規格） ║
    // ╚══════════════════════════════════════╝
    @Override
    @Transactional
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

        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("商品價格不能為空或小於0");
        }

        validateVariants(request.getVariants());

        // 建立真正要存進資料庫的 Product Entity
        Product product = new Product();

        // 把 Request DTO 的資料放進 Product
        product.setName(request.getName());
        product.setPattern(request.getPattern());
        product.setCategoryType(request.getCategoryType());
        product.setStyle(request.getStyle());
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

        // 建立規格並掛到商品上
        for (ProductVariantRequest v : request.getVariants()) {
            ProductVariant variant = toVariantEntity(product, v);
            product.getVariants().add(variant);
        }

        // 存進資料庫（cascade 一併儲存變體）
        Product savedProduct = productRepository.save(product);

        // Entity → Response DTO，回傳給前端
        return toResponse(savedProduct);

    }

    // ╔══════════════════════════════════════╗
    // ║ 廠商修改商品（重設全部規格） ║
    // ╚══════════════════════════════════════╝
    @Override
    @Transactional
    public ProductResponse updateProduct(Long vendorId, Long productId, ProductRequest request) {

        // 找尋要修改的商品
        Product oldProduct = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("找不到商品 ID:" + productId));

        // 確認這個商品是不是這個廠商的
        if (!oldProduct.getVendor().getVendorId().equals(vendorId)) {
            throw new RuntimeException("你沒有權限修改其他廠商的商品");
        }

        if (request.getName() == null || request.getName().isBlank()) {
            throw new RuntimeException("商品名稱不能為空");
        }

        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("商品價格不能為空或小於0");
        }

        validateVariants(request.getVariants());

        // 把前端傳來的新資料放進舊商品
        oldProduct.setName(request.getName());
        oldProduct.setPattern(request.getPattern());
        oldProduct.setCategoryType(request.getCategoryType());
        oldProduct.setStyle(request.getStyle());
        oldProduct.setPrice(request.getPrice());
        oldProduct.setDescription(request.getDescription());
        oldProduct.setImagesJpg(request.getImagesJpg());
        oldProduct.setOutfitPng(request.getOutfitPng());

        // 商品總開關只允許 ACTIVE / INACTIVE（更新時不再允許 DRAFT 上架中途狀態）
        String status = request.getStatus();
        if ("ACTIVE".equals(status) || "INACTIVE".equals(status)) {
            oldProduct.setStatus(status);
        }

        // 規格同步：以 (color, size) 為 key 做 in-place 更新
        // 避免 clear() + orphanRemoval 刪除被 CartItem/OrderItem 引用的規格導致 FK 衝突
        Map<String, ProductVariant> existingMap = new java.util.LinkedHashMap<>();
        for (ProductVariant v : oldProduct.getVariants()) {
            existingMap.put(v.getColor() + "|" + v.getSize(), v);
        }
        for (ProductVariantRequest vr : request.getVariants()) {
            String key = vr.getColor() + "|" + vr.getSize();
            ProductVariant existing = existingMap.get(key);
            if (existing != null) {
                existing.setStock(vr.getStock());
                existing.setImagesJpg(vr.getImagesJpg());
                existing.setOutfitPng(vr.getOutfitPng());
                existing.setStatus(vr.getStatus() == null || vr.getStatus().isBlank()
                        ? "ACTIVE" : vr.getStatus());
            } else {
                oldProduct.getVariants().add(toVariantEntity(oldProduct, vr));
            }
        }

        // 更新資料庫
        Product savedProduct = productRepository.save(oldProduct);

        // Entity → DTO
        return toResponse(savedProduct);

    }

    // 修改單一規格庫存
    @Override
    @Transactional
    public ProductVariantResponse updateVariantStock(Long vendorId, Long variantId, StockRequest request) {

        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("找不到規格 ID:" + variantId));

        // 確認規格屬於這個廠商
        if (!variant.getProduct().getVendor().getVendorId().equals(vendorId)) {
            throw new RuntimeException("你沒有權限修改其他廠商的商品規格");
        }

        Integer stock = request.getStock();

        // 檢查庫存數量是否為空或小於0
        if (stock == null || stock < 0) {
            throw new RuntimeException("庫存數量不能為空或小於0");
        }

        variant.setStock(stock);

        ProductVariant saved = productVariantRepository.save(variant);

        return toVariantResponse(saved);
    }

    // 補貨：原庫存 + 本次補貨數量（只能補自己的規格）
    @Override
    @Transactional
    public ProductVariantResponse replenishVariantStock(Long vendorId, Long variantId, ReplenishRequest request) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("找不到規格 ID:" + variantId));

        // 確認規格屬於這個廠商
        if (!variant.getProduct().getVendor().getVendorId().equals(vendorId)) {
            throw new RuntimeException("你沒有權限補貨其他廠商的商品規格");
        }

        Integer quantity = request.getQuantity();
        if (quantity == null || quantity < 1) {
            throw new RuntimeException("本次補貨數量必須為大於 0 的整數");
        }

        int current = variant.getStock() == null ? 0 : variant.getStock();
        variant.setStock(current + quantity);

        return toVariantResponse(productVariantRepository.save(variant));
    }

    // 修改單一規格販售狀態（停售/恢復單一規格）
    @Override
    @Transactional
    public ProductVariantResponse updateVariantStatus(Long vendorId, Long variantId, String status) {

        if (status == null || status.isBlank()) {
            throw new RuntimeException("規格狀態不能為空");
        }
        if (!"ACTIVE".equals(status) && !"INACTIVE".equals(status)) {
            throw new RuntimeException("規格狀態只能選擇可販售或停售");
        }

        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("找不到規格 ID:" + variantId));

        if (!variant.getProduct().getVendor().getVendorId().equals(vendorId)) {
            throw new RuntimeException("你沒有權限修改其他廠商的商品規格");
        }

        variant.setStatus(status);

        return toVariantResponse(productVariantRepository.save(variant));
    }

    // 整批停售/恢復：依顏色 或 依尺寸
    @Override
    @Transactional
    public List<ProductVariantResponse> batchUpdateVariantStatus(
            Long vendorId, Long productId, String color, String size, String status) {

        if (status == null || status.isBlank()) {
            throw new RuntimeException("規格狀態不能為空");
        }
        if (!"ACTIVE".equals(status) && !"INACTIVE".equals(status)) {
            throw new RuntimeException("規格狀態只能選擇可販售或停售");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("找不到商品 ID:" + productId));
        if (!product.getVendor().getVendorId().equals(vendorId)) {
            throw new RuntimeException("你沒有權限修改其他廠商的商品規格");
        }

        List<ProductVariant> targets;
        boolean hasColor = color != null && !color.isBlank();
        boolean hasSize = size != null && !size.isBlank();

        if (hasColor && hasSize) {
            // 指定顏色＋尺寸 → 單一規格組合
            targets = productVariantRepository.findByProduct_ProductIdAndColorAndSize(productId, color, size);
        } else if (hasColor) {
            // 停售/恢復整批「某顏色」（黑不賣、白照賣）
            targets = productVariantRepository.findByProduct_ProductIdAndColor(productId, color);
        } else if (hasSize) {
            // 停售/恢復整批「某尺寸」（M 不賣、L 照賣）
            targets = productVariantRepository.findByProduct_ProductIdAndSize(productId, size);
        } else {
            throw new RuntimeException("請指定要整批套用的顏色或尺寸");
        }

        List<ProductVariantResponse> result = new ArrayList<>();
        for (ProductVariant variant : targets) {
            variant.setStatus(status);
            result.add(toVariantResponse(productVariantRepository.save(variant)));
        }
        return result;
    }

    // 廠商上架自己產品,上架按鈕
    @Override
    @Transactional
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
    @Transactional
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
    @Transactional
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

    // 下架商品改為待上架
    @Override
    @Transactional
    public ProductResponse setToDraftProduct(Long vendorId, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("找不到商品 ID：" + productId));
        if (!product.getVendor().getVendorId().equals(vendorId)) {
            throw new RuntimeException("你沒有權限修改其他廠商的商品");
        }
        product.setStatus("DRAFT");
        return toResponse(productRepository.save(product));
    }

    // 批次下架
    @Override
    @Transactional
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

    // 低庫存查詢：商品底下有任一規格庫存 ≤5 即納入 (固定每頁10筆)
    @Override
    public SelectPartOfData.Result<ProductResponse> findLowStockProducts(Long vendorId, int page) {
        List<ProductVariant> lowVariants = productVariantRepository
                .findByProduct_Vendor_VendorIdAndStockLessThanEqual(vendorId, 5);

        Set<Product> lowProducts = new LinkedHashSet<>();
        for (ProductVariant variant : lowVariants) {
            lowProducts.add(variant.getProduct());
        }

        List<ProductResponse> all = lowProducts.stream()
                .map(this::toResponse)
                .toList();
        return SelectPartOfData.pageOf10(all, page);
    }

    // ╔══════════════════════════════════════╗
    // ║ Private - 規格驗證與轉換 ║
    // ╚══════════════════════════════════════╝

    private void validateVariants(List<ProductVariantRequest> variants) {
        if (variants == null || variants.isEmpty()) {
            throw new RuntimeException("請至少建立一種商品規格（顏色＋尺寸）");
        }
        for (ProductVariantRequest v : variants) {
            if (v.getColor() == null || v.getColor().isBlank()) {
                throw new RuntimeException("商品顏色不能為空");
            }
            if (v.getSize() == null || v.getSize().isBlank()) {
                throw new RuntimeException("商品尺寸不能為空");
            }
            if (v.getStock() == null || v.getStock() < 0) {
                throw new RuntimeException("庫存數量不能為空或小於0");
            }
        }
    }

    private ProductVariant toVariantEntity(Product product, ProductVariantRequest v) {
        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setColor(v.getColor());
        variant.setSize(v.getSize());
        variant.setStock(v.getStock());
        variant.setImagesJpg(v.getImagesJpg());
        variant.setOutfitPng(v.getOutfitPng());
        variant.setStatus(v.getStatus() == null || v.getStatus().isBlank()
                ? "ACTIVE"
                : v.getStatus());
        return variant;
    }

    private ProductResponse toResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setProductId(product.getProductId());
        response.setName(product.getName());
        response.setPattern(product.getPattern());
        response.setCategoryType(product.getCategoryType());
        response.setStyle(product.getStyle());
        response.setPrice(product.getPrice());
        response.setDescription(product.getDescription());
        response.setImagesJpg(product.getImagesJpg());
        response.setOutfitPng(product.getOutfitPng());
        response.setStatus(product.getStatus());

        if (product.getVendor() != null) {
            response.setVendorId(product.getVendor().getVendorId());
            response.setVendorName(product.getVendor().getVendorName());
        }

        List<ProductVariantResponse> variants = new ArrayList<>();
        if (product.getVariants() != null) {
            for (ProductVariant variant : product.getVariants()) {
                variants.add(toVariantResponse(variant));
            }
        }
        response.setVariants(variants);

        return response;
    }

    private ProductVariantResponse toVariantResponse(ProductVariant variant) {
        return new ProductVariantResponse(
                variant.getVariantId(),
                variant.getProduct() == null ? null : variant.getProduct().getProductId(),
                variant.getColor(),
                variant.getSize(),
                variant.getStock(),
                variant.getImagesJpg(),
                variant.getOutfitPng(),
                variant.getStatus());
    }

}
package com.example.demo.exception;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.Specification;

import com.example.demo.model.Product;

public class ProductSpecification {

    // =====================================================
    // 1. 商品必須是上架狀態 ACTIVE
    // =====================================================
    public static Specification<Product> isActive() {

        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(
                root.get("status"),
                "ACTIVE");
    }

    // =====================================================
    // 2. 商品所屬的 Vendor 也必須是 ACTIVE
    // =====================================================
    public static Specification<Product> vendorIsActive() {

        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(
                root.get("vendor").get("status"),
                "ACTIVE");
    }

    // =====================================================
    // 3. 商品分類
    // 例如：上衣、褲子、洋裝、外套
    // =====================================================
    public static Specification<Product> hasCategoryType(
            String categoryType) {

        return (root, query, criteriaBuilder) -> {

            // 沒有選分類，就不限制
            if (categoryType == null || categoryType.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("categoryType"),
                    categoryType);
        };
    }

    // =====================================================
    // 4. 商品風格
    // 例如：韓系、休閒、正式、街頭
    // =====================================================
    public static Specification<Product> hasStyle(
            String style) {

        return (root, query, criteriaBuilder) -> {

            if (style == null || style.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("style"),
                    style);
        };
    }

    // =====================================================
    // 5. 商品顏色
    // 例如：白色、黑色、藍色
    // =====================================================
    public static Specification<Product> hasColor(
            String color) {

        return (root, query, criteriaBuilder) -> {

            if (color == null || color.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("color"),
                    color);
        };
    }

    // =====================================================
    // 6. 商品尺寸
    // 例如：S、M、L、XL
    // =====================================================
    public static Specification<Product> hasSize(
            String size) {

        return (root, query, criteriaBuilder) -> {

            if (size == null || size.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("size"),
                    size);
        };
    }

    // =====================================================
    // 7. 商品圖案
    // 例如：素色、條紋、格紋、碎花
    // =====================================================
    public static Specification<Product> hasPattern(
            String pattern) {

        return (root, query, criteriaBuilder) -> {

            if (pattern == null || pattern.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("pattern"),
                    pattern);
        };
    }

    // =====================================================
    // 8. 商品名稱模糊搜尋
    // 例如輸入「襯衫」
    // 可以找到「韓系白色襯衫」
    // =====================================================
    public static Specification<Product> nameContains(
            String keyword) {

        return (root, query, criteriaBuilder) -> {

            if (keyword == null || keyword.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.like(
                    root.get("name"),
                    "%" + keyword + "%");
        };
    }

    // =====================================================
    // 8-1. 商品性別
    // 例如：MEN（男裝）、WOMEN（女裝）、KIDS（童裝）
    // =====================================================
    public static Specification<Product> hasGender(String gender) {

        return (root, query, criteriaBuilder) -> {

            if (gender == null || gender.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("gender"),
                    gender);
        };
    }

    // =====================================================
    // 9. 最低價格
    // 例如：500 元以上
    // =====================================================
    public static Specification<Product> priceGreaterThanOrEqual(
            BigDecimal minPrice) {

        return (root, query, criteriaBuilder) -> {

            // 沒有輸入最低價格就不限制
            if (minPrice == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.greaterThanOrEqualTo(
                    root.get("price"),
                    minPrice);
        };
    }

    // =====================================================
    // 10. 最高價格
    // 例如：1000 元以下
    // =====================================================
    public static Specification<Product> priceLessThanOrEqual(
            BigDecimal maxPrice) {

        return (root, query, criteriaBuilder) -> {

            // 沒有輸入最高價格就不限制
            if (maxPrice == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.lessThanOrEqualTo(
                    root.get("price"),
                    maxPrice);
        };
    }

    // 商城按廠商搜尋
    public static Specification<Product> hasVendorId(Long vendorId) {
        return (root, query, criteriaBuilder) -> {

            // 沒傳vendorId,就不限制廠商
            if (vendorId == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(root.get("vendor").get("vendorId"), vendorId);
        };
    }

    // 管理員使用,依商品狀態篩選
    public static Specification<Product> hasStatus(String status) {

        return (root, query, criteriaBuilder) -> {

            // 管理員沒有選狀態
            // → ACTIVE、DRAFT、INACTIVE 全部都可以
            if (status == null || status.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            // 有選狀態
            // → 只查指定狀態
            return criteriaBuilder.equal(
                    root.get("status"),
                    status);
        };
    }

    // 這件商品廠商的合約時間是否大於等於現在時間
    public static Specification<Product> vendorContractNotExpired() {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("vendor").get("contractExpiresAt"),
                LocalDateTime.now());
    }

}
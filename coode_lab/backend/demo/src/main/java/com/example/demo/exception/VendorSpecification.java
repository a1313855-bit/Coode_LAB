package com.example.demo.exception;

import org.springframework.data.jpa.domain.Specification;

import com.example.demo.model.Vendor;

public class VendorSpecification {

    // 搜尋廠商名稱或 Email
    public static Specification<Vendor> keywordContains(String keyword) {

        return (root, query, criteriaBuilder) -> {

            // 沒有輸入關鍵字，就不限制
            if (keyword == null || keyword.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String searchKeyword = "%" + keyword + "%";

            return criteriaBuilder.or(
                    criteriaBuilder.like(
                            root.get("vendorName"),
                            searchKeyword),
                    criteriaBuilder.like(
                            root.get("email"),
                            searchKeyword));
        };
    }

    // 根據廠商狀態篩選
    public static Specification<Vendor> hasStatus(String status) {

        return (root, query, criteriaBuilder) -> {

            // 沒有選狀態，就不限制
            if (status == null || status.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("status"),
                    status);
        };
    }
}
package com.example.demo.util;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * 分頁工具：查詢大量資料時，只顯示其中一部分（例如 100 筆只顯示 10 筆），
 * 並可依 page 切換到下一頁／上一頁。
 *
 * 提供兩種用法：
 *  1. fromList(...)：先把全部資料取回成 List，再手動切頁（簡單、適合小資料量）。
 *  2. of(...)：直接吃 Spring Data 的 Page，適合搭配 Repository 分頁查詢（大資料量建議）。
 */
public final class SelectPartOfData {

    private SelectPartOfData() {
    }

    /**
     * 分頁結果封裝。
     *
     * @param <T> 資料型別
     */
    public static class Result<T> {

        private final List<T> content;
        private final int page;
        private final int size;
        private final long totalElements;
        private final int totalPages;
        private final boolean last;

        public Result(List<T> content, int page, int size, long totalElements, int totalPages, boolean last) {
            this.content = content;
            this.page = page;
            this.size = size;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.last = last;
        }

        public List<T> getContent() {
            return content;
        }

        public int getPage() {
            return page;
        }

        public int getSize() {
            return size;
        }

        public long getTotalElements() {
            return totalElements;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public boolean isLast() {
            return last;
        }

        /** 是否還有下一頁。 */
        public boolean hasNext() {
            return page < totalPages - 1;
        }

        /** 取得下一頁的頁碼（已是最後一頁時回傳目前頁）。 */
        public int nextPage() {
            return hasNext() ? page + 1 : page;
        }
    }

    /**
     * 從完整 List 手動切頁。
     *
     * @param data   全部資料
     * @param page   頁碼（從 0 開始；0 = 第一頁）
     * @param size   每頁筆數（例如 10）
     * @return 分頁結果
     */
    public static <T> Result<T> fromList(List<T> data, int page, int size) {
        long totalElements = (data == null) ? 0 : data.size();
        if (size <= 0) {
            size = 10;
        }
        int totalPages = (int) Math.ceil((double) totalElements / size);
        if (totalPages == 0) {
            totalPages = 1;
        }
        if (page < 0) {
            page = 0;
        }
        if (page >= totalPages) {
            page = totalPages - 1;
        }

        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, (int) totalElements);
        List<T> content = (data == null || data.isEmpty()) ? List.of()
                : data.subList(fromIndex, toIndex);

        boolean last = (page == totalPages - 1);
        return new Result<>(content, page, size, totalElements, totalPages, last);
    }

    /** 固定每頁筆數：10。 */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 從 Spring Data 的 Page 產生分頁結果。
     *
     * @param page Spring Data 分頁查詢結果
     * @param pageable 原本使用的 Pageable（決定 page 與 size）
     * @return 分頁結果
     */
    public static <T> Result<T> of(Page<T> page, Pageable pageable) {
        return new Result<>(
                page.getContent(),
                pageable.getPageNumber(),
                pageable.getPageSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast());
    }

    /**
     * 固定每頁 10 筆，從完整 List 手動切頁。
     *
     * @param data 全部資料
     * @param page 頁碼（從 0 開始；0 = 第一頁）
     * @return 分頁結果（每頁固定 10 筆）
     */
    public static <T> Result<T> pageOf10(List<T> data, int page) {
        return fromList(data, page, DEFAULT_PAGE_SIZE);
    }

    /**
     * 固定每頁 10 筆，從 Spring Data 的 Page 產生分頁結果。
     *
     * @param page Spring Data 分頁查詢結果
     * @return 分頁結果（每頁固定 10 筆）
     */
    public static <T> Result<T> pageOf10(Page<T> page) {
        return new Result<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast());
    }

    /**
     * 便利方法：建立 PageRequest（從 0 開始的 page 與 size），
     * 供 Repository 分頁查詢使用。空參數時給預設值。
     *
     * @param page 頁碼（0 = 第一頁）
     * @param size 每頁筆數
     * @return Pageable
     */
    public static Pageable pageableOf(Integer page, Integer size) {
        int safePage = (page == null || page < 0) ? 0 : page;
        int safeSize = (size == null || size <= 0) ? 10 : size;
        return PageRequest.of(safePage, safeSize);
    }
}

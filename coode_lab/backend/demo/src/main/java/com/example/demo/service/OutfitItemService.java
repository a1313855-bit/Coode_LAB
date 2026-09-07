package com.example.demo.service;

import java.util.List;

// ========== Model ==========
import com.example.demo.model.OutfitItem;

// ========== DTO ==========
import com.example.demo.dto.OutfitItemResponse;

// ========== Util ==========
import com.example.demo.util.SelectPartOfData;

/**
 * OutfitItem 業務邏輯介面。
 *
 * 負責穿搭商品的加入、查詢、替換、移除與清空。
 * 商品位置由 Product.categoryType 決定；
 * 試穿顏色由 ProductVariant（規格）決定。
 */
public interface OutfitItemService {

    /**
     * 將商品加入穿搭。
     *
     * 商品的穿搭位置由 Product.categoryType 自動判斷。
     * 規格（顏色）用於決定試穿圖。
     *
     * @param outfitId  穿搭 ID
     * @param productId 商品 ID
     * @param variantId 規格 ID（顏色）
     * @return 新增或替換完成的 OutfitItem
     */
    OutfitItem addItem(
            Long outfitId,
            Long productId,
            Long variantId
    );

    /**
     * 將商品加入穿搭的指定位置。
     *
     * 由前端明確指定 slotType，
     * 讓 TOP 與 OUTER 可以同時存在。
     *
     * @param outfitId  穿搭 ID
     * @param productId 商品 ID
     * @param variantId 規格 ID（顏色）
     * @param slotType  穿搭位置（HEADWEAR / UPPER_BODY / BOTTOM / FULL_BODY）
     * @return 新增或替換完成的 OutfitItem
     */
    OutfitItem addItemWithSlot(
            Long outfitId,
            Long productId,
            Long variantId,
            String slotType
    );

    /**
     * 查詢指定穿搭中的所有商品 (固定每頁10筆,page 從 0 開始)。
     *
     * @param outfitId 穿搭 ID
     * @return OutfitItem 分頁結果
     */
    SelectPartOfData.Result<OutfitItemResponse> findByOutfitId(
            Long outfitId,
            int page
    );

    /**
     * 替換指定穿搭位置上的商品。
     *
     * @param outfitId  穿搭 ID
     * @param productId 商品 ID
     * @param variantId 規格 ID（顏色）
     * @return 替換完成的 OutfitItem
     */
    OutfitItem replaceItem(
            Long outfitId,
            Long productId,
            Long variantId
    );

    /**
     * 移除指定 OutfitItem。
     *
     * @param outfitItemId OutfitItem ID
     */
    void removeItem(
            Long outfitItemId
    );

    /**
     * 清空指定穿搭中的全部商品。
     *
     * @param outfitId 穿搭 ID
     */
    void clearOutfit(
            Long outfitId
    );
}
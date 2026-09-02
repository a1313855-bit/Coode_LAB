package com.example.demo.service;

// ========== Java ==========
import java.util.List;

// ========== Model ==========
import com.example.demo.model.OutfitItem;

// ========== Util ==========
import com.example.demo.util.SelectPartOfData;


/**
 * OutfitItem 業務邏輯介面。
 *
 * 負責穿搭商品的加入、查詢、替換、移除與清空。
 */
public interface OutfitItemService {

    /**
     * 將商品加入穿搭。
     *
     * 商品的穿搭位置由 Product.categoryType 自動判斷。
     *
     * 如果該位置目前沒有商品，則新增 OutfitItem；
     * 如果該位置已有商品，則直接替換原商品。
     *
     * @param outfitId 穿搭 ID
     * @param productId 商品 ID
     * @return 新增或替換完成的 OutfitItem
     */
    OutfitItem addItem(
            Long outfitId,
            Long productId
    );


    /**
     * 將商品加入穿搭的指定位置。
     *
     * 與 addItem() 不同，這個方法由前端明確指定 slotType，
     * 讓 TOP 與 OUTER 可以同時存在（分別放在不同位置）。
     *
     * 如果該位置目前沒有商品，則新增 OutfitItem；
     * 如果該位置已有商品，則直接替換原商品。
     *
     * @param outfitId  穿搭 ID
     * @param productId 商品 ID
     * @param slotType  穿搭位置（TOP / OUTER / BOTTOM / SHOES / ACCESSORY）
     * @return 新增或替換完成的 OutfitItem
     */
    OutfitItem addItemWithSlot(
            Long outfitId,
            Long productId,
            String slotType
    );


    /**
     * 查詢指定穿搭中的所有商品 (固定每頁10筆,page 從 0 開始)。
     *
     * @param outfitId 穿搭 ID
     * @return OutfitItem 分頁結果
     */
    SelectPartOfData.Result<OutfitItem> findByOutfitId(
            Long outfitId,
            int page
    );


    /**
     * 替換指定穿搭位置上的商品。
     *
     * 商品位置由新商品的 Product.categoryType 自動判斷。
     * 如果該位置原本沒有商品，則拋出例外。
     *
     * @param outfitId 穿搭 ID
     * @param productId 新商品 ID
     * @return 替換完成的 OutfitItem
     */
    OutfitItem replaceItem(
            Long outfitId,
            Long productId
    );


    /**
     * 移除指定 OutfitItem。
     *
     * 只刪除 Outfit 與 Product 的穿搭關聯，
     * 不會刪除 Product 本身。
     *
     * @param outfitItemId OutfitItem ID
     */
    void removeItem(
            Long outfitItemId
    );


    /**
     * 清空指定穿搭中的全部商品。
     *
     * Outfit 本身仍會保留。
     *
     * @param outfitId 穿搭 ID
     */
    void clearOutfit(
            Long outfitId
    );
}
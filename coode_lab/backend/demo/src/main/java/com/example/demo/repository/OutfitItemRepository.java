package com.example.demo.repository;

// ========== Java ==========
import java.util.List;
import java.util.Optional;

// ========== Spring Data JPA ==========
import org.springframework.data.jpa.repository.JpaRepository;

// ========== Model ==========
import com.example.demo.model.Outfit;
import com.example.demo.model.OutfitItem;


/**
 * OutfitItemRepository。
 *
 * 負責 OutfitItem 的資料庫存取。
 *
 * 目前試衣間的穿搭位置規則：
 *
 * TOP / OUTER / OUTERWEAR → UPPER_BODY
 * BOTTOM / PANTS / SKIRT → BOTTOM
 * DRESS → FULL_BODY
 * HEADWEAR / HAT → HEADWEAR
 *
 * 同一套 Outfit 的同一個 slotType
 * 最多只允許存在一筆 OutfitItem。
 */

public interface OutfitItemRepository
        extends JpaRepository<OutfitItem, Long> {


    /**
     * 查詢指定穿搭中的所有 OutfitItem。
     *
     * 例如：
     *
     * UPPER_BODY → 白色 T-shirt
     * BOTTOM     → 牛仔褲
     * FULL_BODY  → 黑色連身洋裝
     *
     * @param outfit 要查詢的 Outfit
     * @return 此穿搭中的所有 OutfitItem
     */
    List<OutfitItem> findByOutfit(
            Outfit outfit
    );


    /**
     * 根據 Outfit 與 slotType
     * 查詢指定穿搭位置目前的 OutfitItem。
     *
     * 例如：
     *
     * Outfit：上班穿搭
     * slotType：UPPER_BODY
     *
     * 可以查詢目前上半身位置
     * 是否已經存在商品。
     *
     * TOP 與 OUTER 都會被 Service
     * 轉換成 UPPER_BODY，
     * 因此上衣與外套會查詢同一個位置，
     * 並在加入新商品時互相替換。
     *
     * @param outfit 穿搭
     * @param slotType 穿搭位置
     * @return 該位置的 OutfitItem；不存在時回傳 Optional.empty()
     */
    Optional<OutfitItem> findByOutfitAndSlotType(
            Outfit outfit,
            String slotType
    );


    /**
     * 刪除指定穿搭中的指定位置的 OutfitItem。
     *
     * 用於處理互斥規則：
     * 加入 FULL_BODY（洋裝）時，清除 UPPER_BODY / BOTTOM；
     * 加入 UPPER_BODY 或 BOTTOM 時，清除 FULL_BODY。
     *
     * @param outfit 穿搭
     * @param slotType 要被清除的穿搭位置
     */
    void deleteByOutfitAndSlotType(
            Outfit outfit,
            String slotType
    );


    /**
     * 刪除指定 Outfit 底下的全部 OutfitItem。
     *
     * 只會清除穿搭中的商品關聯，
     * 不會刪除 Outfit 本身，
     * 也不會刪除 Product 本身。
     *
     * @param outfit 要清空的 Outfit
     */
    void deleteAllByOutfit(
            Outfit outfit
    );
}
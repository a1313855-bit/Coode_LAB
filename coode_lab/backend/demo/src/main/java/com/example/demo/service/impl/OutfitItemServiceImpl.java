package com.example.demo.service.impl;

// ========== Java ==========
import java.util.List;
import java.util.Locale;
import java.util.Optional;

// ========== Spring ==========
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// ========== Model ==========
import com.example.demo.model.Outfit;
import com.example.demo.model.OutfitItem;
import com.example.demo.model.Product;

// ========== Repository ==========
import com.example.demo.repository.OutfitItemRepository;
import com.example.demo.repository.ProductRepository;

// ========== Service ==========
import com.example.demo.service.OutfitItemService;
import com.example.demo.service.OutfitService;
import com.example.demo.util.SelectPartOfData;

/**
 * OutfitItemService 的實作類別。
 *
 * 負責處理穿搭商品的加入、查詢、替換、移除與清空。
 *
 * 商品的穿搭位置不由前端指定，
 * 而是依 Product.categoryType 自動判斷。
 *
 * 目前試衣間不支援上衣與外套同時疊加，
 * 因此 TOP 與 OUTER 共用 UPPER_BODY 穿搭位置。
 */
@Service
public class OutfitItemServiceImpl implements OutfitItemService {

        // ╔════════════════╗
        // ║ Slot Constants ║
        // ╚════════════════╝

        private static final String SLOT_UPPER_BODY = "UPPER_BODY";
        private static final String SLOT_BOTTOM = "BOTTOM";
        private static final String SLOT_SHOES = "SHOES";
        private static final String SLOT_ACCESSORY = "ACCESSORY";

        // ╔════════════╗
        // ║ Repository ║
        // ╚════════════╝

        private final OutfitItemRepository outfitItemRepository;
        private final ProductRepository productRepository;

        // ╔═════════╗
        // ║ Service ║
        // ╚═════════╝

        private final OutfitService outfitService;
        // private final ProductService productService;

        // ╔═════════════╗
        // ║ Constructor ║
        // ╚═════════════╝

        /**
         * 建構 OutfitItemServiceImpl。
         *
         * @param outfitItemRepository OutfitItem 資料存取元件
         * @param outfitService        Outfit 業務邏輯元件
         * @param productService       Product 業務邏輯元件
         */
        public OutfitItemServiceImpl(
                        OutfitItemRepository outfitItemRepository,
                        OutfitService outfitService,
                        ProductRepository productRepository) {
                // ProductService productService

                this.outfitItemRepository = outfitItemRepository;
                this.outfitService = outfitService;
                this.productRepository = productRepository;
                // this.productService = productService;
        }

        // ╔══════════════════════╗
        // ║ Add / Select Product ║
        // ╚══════════════════════╝

        /**
         * 將商品加入指定穿搭。
         *
         * 商業邏輯：
         * 1、確認 Outfit 存在。
         * 2、確認 Product 存在。
         * 3、根據 Product.categoryType 自動決定 slotType。
         * 4、查詢該 slotType 是否已有商品。
         * 5、沒有商品時建立新的 OutfitItem。
         * 6、已有商品時直接以新商品替換。
         *
         * TOP 與 OUTER 目前共用 UPPER_BODY，
         * 因此兩者會互相替換。
         *
         * @param outfitId  穿搭 ID
         * @param productId 商品 ID
         * @return 新增或替換完成的 OutfitItem
         */
        @Override
        @Transactional
        public OutfitItem addItem(
                        Long outfitId,
                        Long productId) {

                // 1、確認 Outfit 存在
                Outfit outfit = getOutfit(outfitId);

                // 2、確認 Product 存在
                Product product = getProduct(productId);

                // 3、依商品分類決定穿搭位置
                String slotType = resolveSlotType(product);

                /*
                 * 4、查詢該穿搭位置目前是否已有商品。
                 *
                 * TOP 與 OUTER 都會得到 UPPER_BODY，
                 * 因此會查詢到同一個 OutfitItem。
                 */
                Optional<OutfitItem> existingItem = outfitItemRepository
                                .findByOutfitAndSlotType(
                                                outfit,
                                                slotType);

                /*
                 * 5、該位置已經有商品
                 * → 不新增第二筆
                 * → 直接替換 Product。
                 */
                if (existingItem.isPresent()) {

                        OutfitItem outfitItem = existingItem.get();

                        outfitItem.setProduct(product);

                        return outfitItemRepository
                                        .save(outfitItem);
                }

                /*
                 * 6、該位置目前沒有商品
                 * → 建立新的 OutfitItem。
                 */
                OutfitItem outfitItem = new OutfitItem();

                outfitItem.setOutfit(outfit);
                outfitItem.setProduct(product);
                outfitItem.setSlotType(slotType);

return outfitItemRepository
                                .save(outfitItem);
        }

        // ╔═════════════════════╗
        // ║ Add Item With Slot ║
        // ╚═════════════════════╝

        /**
         * 將商品加入穿搭的指定位置。
         *
         * 與 addItem() 不同，此方法由前端明確指定 slotType，
         * 因此 TOP 與 OUTER 可以同時存在：
         *
         * TOP → 自己的 TOP 位置
         * OUTER → 自己的 OUTER 位置
         *
         * @param outfitId  穿搭 ID
         * @param productId 商品 ID
         * @param slotType  穿搭位置
         * @return 新增或替換完成的 OutfitItem
         */
        @Override
        @Transactional
        public OutfitItem addItemWithSlot(
                        Long outfitId,
                        Long productId,
                        String slotType) {

                // 1、確認 Outfit 存在
                Outfit outfit = getOutfit(outfitId);

                // 2、確認 Product 存在
                Product product = getProduct(productId);

                // 3、檢查並整理穿搭位置
                String normalizedSlot = validateSlot(slotType);

                /*
                 * 4、查詢該位置目前是否已有商品。
                 */
                Optional<OutfitItem> existingItem = outfitItemRepository
                                .findByOutfitAndSlotType(
                                                outfit,
                                                normalizedSlot);

                /*
                 * 5、該位置已商品 → 直接替換。
                 */
                if (existingItem.isPresent()) {

                        OutfitItem outfitItem = existingItem.get();

                        outfitItem.setProduct(product);

                        return outfitItemRepository
                                        .save(outfitItem);
                }

                /*
                 * 6、該位置目前沒有商品 → 建立新的 OutfitItem。
                 */
                OutfitItem outfitItem = new OutfitItem();

                outfitItem.setOutfit(outfit);
                outfitItem.setProduct(product);
                outfitItem.setSlotType(normalizedSlot);

                return outfitItemRepository
                                .save(outfitItem);
        }

        // ╔══════════════════════╗
        // ║ Find Outfit Items    ║
        // ╚══════════════════════╝

        /**
         * 查詢指定穿搭中的全部商品。
         *
         * @param outfitId 穿搭 ID
         * @return OutfitItem 清單
         */
        @Override
        @Transactional(readOnly = true)
        public SelectPartOfData.Result<OutfitItem> findByOutfitId(
                        Long outfitId,
                        int page) {

                // 1、確認 Outfit 存在
                Outfit outfit = getOutfit(outfitId);

                // 2、查詢此穿搭的所有 OutfitItem
                List<OutfitItem> all = outfitItemRepository
                                .findByOutfit(outfit);
                return SelectPartOfData.pageOf10(all, page);
        }

        // ╔══════════════════════╗
        // ║ Replace Item ║
        // ╚══════════════════════╝

        /**
         * 替換指定穿搭位置目前的商品。
         *
         * 新商品要替換哪個位置，
         * 由 Product.categoryType 自動判斷。
         *
         * addItem()：
         * 沒有商品 → 新增
         * 已有商品 → 替換
         *
         * replaceItem()：
         * 已有商品 → 替換
         * 沒有商品 → 拋出例外
         *
         * @param outfitId  穿搭 ID
         * @param productId 新商品 ID
         * @return 替換完成的 OutfitItem
         */
        @Override
        @Transactional
        public OutfitItem replaceItem(
                        Long outfitId,
                        Long productId) {

                // 1、確認 Outfit 存在
                Outfit outfit = getOutfit(outfitId);

                // 2、確認新 Product 存在
                Product newProduct = getProduct(productId);

                // 3、依新商品分類決定穿搭位置
                String slotType = resolveSlotType(newProduct);

                // 4、找到該位置目前的 OutfitItem
                OutfitItem existingItem = outfitItemRepository
                                .findByOutfitAndSlotType(
                                                outfit,
                                                slotType)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "此穿搭的 "
                                                                + slotType
                                                                + " 位置目前沒有商品"));

                // 5、替換商品
                existingItem.setProduct(newProduct);

                // 6、儲存修改
                return outfitItemRepository
                                .save(existingItem);
        }

        // ╔══════════════════════╗
        // ║ Remove Item ║
        // ╚══════════════════════╝

        /**
         * 移除指定 OutfitItem。
         *
         * 只刪除穿搭關聯，
         * 不會刪除 Product 本身。
         *
         * @param outfitItemId OutfitItem ID
         */
        @Override
        @Transactional
        public void removeItem(
                        Long outfitItemId) {

                // 1、檢查 OutfitItem ID
                if (outfitItemId == null) {
                        throw new IllegalArgumentException(
                                        "穿搭商品 ID 不得為空");
                }

                // 2、確認 OutfitItem 存在
                OutfitItem outfitItem = outfitItemRepository
                                .findById(outfitItemId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "找不到穿搭商品資料"));

                // 3、刪除 OutfitItem
                outfitItemRepository
                                .delete(outfitItem);
        }

        // ╔══════════════════════╗
        // ║ Clear Outfit ║
        // ╚══════════════════════╝

        /**
         * 清空指定穿搭中的全部商品。
         *
         * Outfit 本身仍會保留，
         * Product 本身也不會被刪除。
         *
         * @param outfitId 穿搭 ID
         */
        @Override
        @Transactional
        public void clearOutfit(
                        Long outfitId) {

                // 1、確認 Outfit 存在
                Outfit outfit = getOutfit(outfitId);

                // 2、刪除此 Outfit 底下全部 OutfitItem
                outfitItemRepository
                                .deleteAllByOutfit(outfit);
        }

        // ╔════════════════════════╗
        // ║ Private - Get Outfit ║
        // ╚════════════════════════╝

        /**
         * 根據 outfitId 取得 Outfit。
         *
         * Outfit 的查詢交由 OutfitService 負責。
         */
        private Outfit getOutfit(
                        Long outfitId) {

                if (outfitId == null) {
                        throw new IllegalArgumentException(
                                        "穿搭 ID 不得為空");
                }

                return outfitService
                                .findById(outfitId);
        }

        // ╔═════════════════════════╗
        // ║ Private - Get Product ║
        // ╚═════════════════════════╝

        /**
         * 根據 productId 取得 Product。
         *
         * Product 的查詢交由 ProductService 負責。
         */
        private Product getProduct(
                        Long productId) {

                if (productId == null) {
                        throw new IllegalArgumentException(
                                        "商品 ID 不得為空");
                }

                return productRepository
                                .findById(productId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "找不到商品資料"));
        }

        // ╔════════════════════════════╗
        // ║ Private - Resolve Slot ║
        // ╚════════════════════════════╝

        /**
         * 根據商品分類決定試衣間穿搭位置。
         *
         * 商品分類與穿搭位置是兩個不同概念。
         *
         * Product.categoryType：
         * TOP
         * OUTER
         * BOTTOM
         * SHOES
         * ACCESSORY
         *
         * OutfitItem.slotType：
         * UPPER_BODY
         * BOTTOM
         * SHOES
         * ACCESSORY
         *
         * TOP 與 OUTER 共用 UPPER_BODY，
         * 因此目前無法同時穿著，會互相替換。
         *
         * @param product 商品
         * @return 對應的 slotType
         */
        private String resolveSlotType(
                        Product product) {

                // 1、確認 Product
                if (product == null) {
                        throw new IllegalArgumentException(
                                        "商品不得為空");
                }

                // 2、確認商品分類存在
                if (product.getCategoryType() == null) {
                        throw new IllegalArgumentException(
                                        "商品尚未設定分類，無法加入穿搭");
                }

                /*
                 * 同時相容：
                 *
                 * private CategoryType categoryType;
                 *
                 * 以及：
                 *
                 * private String categoryType;
                 *
                 * 避免合併時因為 Enum / String 不同而修改大量程式。
                 */
                Object rawCategory = product.getCategoryType();

                String categoryType;

                if (rawCategory instanceof Enum<?>) {

                        categoryType = ((Enum<?>) rawCategory)
                                        .name();

                } else {

                        categoryType = rawCategory.toString();
                }

                categoryType = categoryType
                                .trim()
                                .toUpperCase(Locale.ROOT);

                /*
                 * 3、將商品分類轉換成穿搭位置。
                 */
                switch (categoryType) {

                        /*
                         * TOP 與 OUTER 共用上半身位置。
                         *
                         * 例如：
                         * TOP → 白 T
                         *
                         * 使用者再點 OUTER → 外套
                         *
                         * 最終：
                         * UPPER_BODY → 外套
                         */
                        case "TOP":
                        case "OUTER":
                        case "OUTERWEAR":
                                return SLOT_UPPER_BODY;

                        case "BOTTOM":
                                return SLOT_BOTTOM;

                        case "SHOES":
                                return SLOT_SHOES;

                        case "ACCESSORY":
                                return SLOT_ACCESSORY;

                        default:
                                throw new IllegalArgumentException(
                                                "商品分類 "
                                                                + categoryType
                                                                + " 不支援試衣間功能");
                }
        }

        // ╔═════════════════════════╗
        // ║ Private - Validate Slot ║
        // ╚═════════════════════════╝

        /**
         * 檢查前端指定的穿搭位置是否合法。
         *
         * @param slotType 前端傳入的穿搭位置
         * @return 大寫的合法位置
         */
        private String validateSlot(String slotType) {

                if (slotType == null || slotType.isBlank()) {
                        throw new IllegalArgumentException(
                                        "穿搭位置不得為空");
                }

                String normalized = slotType
                                .trim()
                                .toUpperCase(Locale.ROOT);

                switch (normalized) {
                        case "TOP":
                        case "OUTER":
                        case "BOTTOM":
                        case "SHOES":
                        case "ACCESSORY":
                                return normalized;
                        default:
                                throw new IllegalArgumentException(
                                                "穿搭位置 " + slotType + " 不合法");
                }
        }
}

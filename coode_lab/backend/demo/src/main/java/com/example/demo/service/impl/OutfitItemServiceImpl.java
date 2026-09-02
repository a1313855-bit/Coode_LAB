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
import com.example.demo.model.ProductVariant;

// ========== DTO ==========
import com.example.demo.dto.OutfitItemResponse;

// ========== Repository ==========
import com.example.demo.repository.OutfitItemRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.ProductVariantRepository;

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
 * 試穿顏色由 ProductVariant（規格）決定；
 * 儲存穿搭時一併記錄「哪件商品、哪個顏色」。
 *
 * 目前試衣間提供四個穿搭位置：
 * HEADWEAR（帽子/頭飾）、UPPER_BODY（上衣/外套）、
 * BOTTOM（下身）、FULL_BODY（洋裝）。
 *
 * TOP / OUTER / OUTERWEAR 共用 UPPER_BODY；
 * BOTTOM / PANTS / SKIRT 共用 BOTTOM；
 * HEADWEAR / HAT 共用 HEADWEAR；
 * DRESS 使用 FULL_BODY。
 *
 * 洋裝 FULL_BODY 與 UPPER_BODY / BOTTOM 互斥；
 * SHOES / ACCESSORY 不支援試衣間。
 */
@Service
public class OutfitItemServiceImpl implements OutfitItemService {

        // ╔════════════════╗
        // ║ Slot Constants ║
        // ╚════════════════╝

        private static final String SLOT_HEADWEAR = "HEADWEAR";
        private static final String SLOT_UPPER_BODY = "UPPER_BODY";
        private static final String SLOT_BOTTOM = "BOTTOM";
        private static final String SLOT_FULL_BODY = "FULL_BODY";

        // ╔════════════╗
        // ║ Repository ║
        // ╚════════════╝

        private final OutfitItemRepository outfitItemRepository;
        private final ProductRepository productRepository;
        private final ProductVariantRepository productVariantRepository;

        // ╔═════════╗
        // ║ Service ║
        // ╚═════════╝

        private final OutfitService outfitService;

        // ╔═════════════╗
        // ║ Constructor ║
        // ╚═════════════╝

        public OutfitItemServiceImpl(
                        OutfitItemRepository outfitItemRepository,
                        OutfitService outfitService,
                        ProductRepository productRepository,
                        ProductVariantRepository productVariantRepository) {

                this.outfitItemRepository = outfitItemRepository;
                this.outfitService = outfitService;
                this.productRepository = productRepository;
                this.productVariantRepository = productVariantRepository;
        }

        // ╔══════════════════════╗
        // ║ Add / Select Product ║
        // ╚══════════════════════╝

        /**
         * 將商品加入指定穿搭。
         *
         * 依 Product.categoryType 自動決定 slotType，
         * variantId 記錄所選顏色，以決定試穿圖。
         */
        @Override
        @Transactional
        public OutfitItem addItem(
                        Long outfitId,
                        Long productId,
                        Long variantId) {

                Outfit outfit = getOutfit(outfitId);

                Product product = getProduct(productId);

                ProductVariant variant = getVariant(product, variantId);

                // 依商品分類決定穿搭位置
                String slotType = resolveSlotType(product);

                // 處理互斥規則（FULL_BODY vs UPPER_BODY / BOTTOM）
                clearConflictingSlots(outfit, slotType);

                Optional<OutfitItem> existingItem = outfitItemRepository
                                .findByOutfitAndSlotType(
                                                outfit,
                                                slotType);

                if (existingItem.isPresent()) {

                        OutfitItem outfitItem = existingItem.get();

                        outfitItem.setProduct(product);
                        outfitItem.setVariant(variant);

                        return outfitItemRepository
                                        .save(outfitItem);
                }

                OutfitItem outfitItem = new OutfitItem();

                outfitItem.setOutfit(outfit);
                outfitItem.setProduct(product);
                outfitItem.setVariant(variant);
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
         * 因此 TOP 與 OUTER 可以同時存在。
         *
         * variantId 記錄所選顏色，以決定試穿圖。
         */
        @Override
        @Transactional
        public OutfitItem addItemWithSlot(
                        Long outfitId,
                        Long productId,
                        Long variantId,
                        String slotType) {

                Outfit outfit = getOutfit(outfitId);

                Product product = getProduct(productId);

                ProductVariant variant = getVariant(product, variantId);

                String normalizedSlot = validateSlot(slotType);

                // 處理互斥規則（FULL_BODY vs UPPER_BODY / BOTTOM）
                clearConflictingSlots(outfit, normalizedSlot);

                Optional<OutfitItem> existingItem = outfitItemRepository
                                .findByOutfitAndSlotType(
                                                outfit,
                                                normalizedSlot);

                if (existingItem.isPresent()) {

                        OutfitItem outfitItem = existingItem.get();

                        outfitItem.setProduct(product);
                        outfitItem.setVariant(variant);

                        return outfitItemRepository
                                        .save(outfitItem);
                }

                OutfitItem outfitItem = new OutfitItem();

                outfitItem.setOutfit(outfit);
                outfitItem.setProduct(product);
                outfitItem.setVariant(variant);
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
         * 回傳 OutfitItemResponse DTO（含商品名稱與規格顏色/試穿圖）。
         */
        @Override
        @Transactional(readOnly = true)
        public SelectPartOfData.Result<OutfitItemResponse> findByOutfitId(
                        Long outfitId,
                        int page) {

                Outfit outfit = getOutfit(outfitId);

                List<OutfitItemResponse> all = outfitItemRepository
                                .findByOutfit(outfit)
                                .stream()
                                .map(this::toOutfitItemResponse)
                                .toList();
                return SelectPartOfData.pageOf10(all, page);
        }

        // ╔══════════════════════╗
        // ║ Replace Item ║
        // ╚══════════════════════╝

        /**
         * 替換指定穿搭位置目前的商品（含顏色）。
         *
         * 位置由新商品 Product.categoryType 自動判斷。
         */
        @Override
        @Transactional
        public OutfitItem replaceItem(
                        Long outfitId,
                        Long productId,
                        Long variantId) {

                Outfit outfit = getOutfit(outfitId);

                Product newProduct = getProduct(productId);

                ProductVariant variant = getVariant(newProduct, variantId);

                String slotType = resolveSlotType(newProduct);

                OutfitItem existingItem = outfitItemRepository
                                .findByOutfitAndSlotType(
                                                outfit,
                                                slotType)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "此穿搭的 "
                                                                + slotType
                                                                + " 位置目前沒有商品"));

                existingItem.setProduct(newProduct);
                existingItem.setVariant(variant);

                return outfitItemRepository
                                .save(existingItem);
        }

        // ╔══════════════════════╗
        // ║ Remove Item ║
        // ╚══════════════════════╝

        @Override
        @Transactional
        public void removeItem(
                        Long outfitItemId) {

                if (outfitItemId == null) {
                        throw new IllegalArgumentException(
                                        "穿搭商品 ID 不得為空");
                }

                OutfitItem outfitItem = outfitItemRepository
                                .findById(outfitItemId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "找不到穿搭商品資料"));

                outfitItemRepository
                                .delete(outfitItem);
        }

        // ╔══════════════════════╗
        // ║ Clear Outfit ║
        // ╚══════════════════════╝

        @Override
        @Transactional
        public void clearOutfit(
                        Long outfitId) {

                Outfit outfit = getOutfit(outfitId);

                outfitItemRepository
                                .deleteAllByOutfit(outfit);
        }

        // ╔═════════════════════════╗
        // ║ Private - Get Outfit ║
        // ╚═════════════════════════╝

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

        // ╔═════════════════════════╗
        // ║ Private - Get Variant ║
        // ╚═════════════════════════╝

        private ProductVariant getVariant(
                        Product product,
                        Long variantId) {

                if (variantId == null) {
                        throw new IllegalArgumentException(
                                        "規格 ID 不得為空");
                }

                ProductVariant variant = productVariantRepository
                                .findById(variantId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "找不到商品規格資料"));

                if (product != null
                                && variant.getProduct() != null
                                && !variant.getProduct().getProductId().equals(product.getProductId())) {
                        throw new IllegalArgumentException(
                                        "該規格不屬於此商品");
                }

                return variant;
        }

        // ╔══════════════════════════════════╗
        // ║ Private - Entity → Response DTO ║
        // ╚══════════════════════════════════╝

        private OutfitItemResponse toOutfitItemResponse(OutfitItem outfitItem) {
                OutfitItemResponse response = new OutfitItemResponse();
                response.setOutfitItemId(outfitItem.getOutfititemsId());
                response.setSlotType(outfitItem.getSlotType());

                if (outfitItem.getOutfit() != null) {
                        response.setOutfitId(outfitItem.getOutfit().getOutfitId());
                }

                Product product = outfitItem.getProduct();
                if (product != null) {
                        response.setProductId(product.getProductId());
                        response.setProductName(product.getName());
                }

                ProductVariant variant = outfitItem.getVariant();
                if (variant != null) {
                        response.setVariantId(variant.getVariantId());
                        response.setColor(variant.getColor());
                        response.setSize(variant.getSize());
                        response.setVariantImagesJpg(variant.getImagesJpg());
                        response.setVariantOutfitPng(variant.getOutfitPng());
                }

                return response;
        }

        // ╔═════════════════════════╗
        // ║ Private - Resolve Slot ║
        // ╚═════════════════════════╝

        private String resolveSlotType(
                        Product product) {

                if (product == null) {
                        throw new IllegalArgumentException(
                                        "商品不得為空");
                }

                if (product.getCategoryType() == null) {
                        throw new IllegalArgumentException(
                                        "商品尚未設定分類，無法加入穿搭");
                }

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

                switch (categoryType) {

                        case "TOP":
                        case "OUTER":
                        case "OUTERWEAR":
                                return SLOT_UPPER_BODY;

                        case "BOTTOM":
                        case "PANTS":
                        case "SKIRT":
                                return SLOT_BOTTOM;

                        case "DRESS":
                                return SLOT_FULL_BODY;

                        case "HEADWEAR":
                        case "HAT":
                                return SLOT_HEADWEAR;

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

        private String validateSlot(String slotType) {

                if (slotType == null || slotType.isBlank()) {
                        throw new IllegalArgumentException(
                                        "穿搭位置不得為空");
                }

                String normalized = slotType
                                .trim()
                                .toUpperCase(Locale.ROOT);

                switch (normalized) {
                        case "HEADWEAR":
                        case "UPPER_BODY":
                        case "BOTTOM":
                        case "FULL_BODY":
                                return normalized;
                        case "TOP":
                        case "OUTER":
                        case "PANTS":
                        case "SKIRT":
                        case "DRESS":
                        case "HAT":
                                throw new IllegalArgumentException(
                                                "請以商品分類加入試衣間，勿直接指定穿搭位置 " + slotType);
                        default:
                                throw new IllegalArgumentException(
                                                "穿搭位置 " + slotType + " 不合法");
                }
        }

        // ╔══════════════════════════════════════╗
        // ║ Private - Clear Conflicting Slots    ║
        // ╚══════════════════════════════════════╝

        /**
         * 處理試衣間互斥規則。
         *
         * 洋裝（FULL_BODY）佔據上半身與下半身，
         * 因此：
         *
         * 加入 FULL_BODY       → 清除 UPPER_BODY、BOTTOM
         * 加入 UPPER_BODY / BOTTOM → 清除 FULL_BODY
         *
         * （HEADWEAR 獨立於以上規則，不互相影響）
         *
         * @param outfit 穿搭
         * @param slotType 新加入的穿搭位置
         */
        private void clearConflictingSlots(
                        Outfit outfit,
                        String slotType) {

                if (SLOT_FULL_BODY.equals(slotType)) {

                        outfitItemRepository
                                        .deleteByOutfitAndSlotType(
                                                        outfit,
                                                        SLOT_UPPER_BODY);

                        outfitItemRepository
                                        .deleteByOutfitAndSlotType(
                                                        outfit,
                                                        SLOT_BOTTOM);

                } else if (SLOT_UPPER_BODY.equals(slotType)
                                || SLOT_BOTTOM.equals(slotType)) {

                        outfitItemRepository
                                        .deleteByOutfitAndSlotType(
                                                        outfit,
                                                        SLOT_FULL_BODY);
                }
        }
}
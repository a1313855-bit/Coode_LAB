package com.example.demo.controller;

// ========== Java ==========
import java.util.List;

// ========== Spring ==========
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// ========== Validation ==========
import jakarta.validation.Valid;

// ========== DTO ==========
import com.example.demo.dto.OutfitItemResponse;
import com.example.demo.dto.OutfitItemSlotRequest;
import com.example.demo.dto.OutfitItemRequest;

// ========== Model ==========
import com.example.demo.model.OutfitItem;

// ========== Service ==========
import com.example.demo.service.OutfitItemService;

// ========== Util ==========
import com.example.demo.util.SelectPartOfData;


/**
 * OutfitItem REST API。
 *
 * 提供試衣間商品加入、查詢、替換、移除與清空功能。
 */
@RestController
@RequestMapping("/api/outfit-items")
public class OutfitItemController {

    private final OutfitItemService outfitItemService;


    /**
     * 建構 OutfitItemController。
     *
     * @param outfitItemService OutfitItem 業務邏輯元件
     */
    public OutfitItemController(
            OutfitItemService outfitItemService) {

        this.outfitItemService =
                outfitItemService;
    }


    // ╔══════════════════════╗
    // ║ Add / Try On Product ║
    // ╚══════════════════════╝

    /**
     * 將商品加入指定穿搭。
     *
     * 前端只需要提供 outfitId、productId 與 variantId，
     * 不需要自行指定 slotType。
     *
     * 商品位置由後端依 Product.categoryType 自動判斷。
     * variantId 記錄會員挑選的顏色，決定試穿圖。
     *
     * POST
     * /api/outfit-items/outfits/{outfitId}/products/{productId}/variants/{variantId}
     *
     * @param outfitId 穿搭 ID
     * @param productId 商品 ID
     * @param variantId 規格 ID（顏色）
     * @return 新增或替換完成的 OutfitItem
     */
    @PostMapping(
            "/outfits/{outfitId}/products/{productId}/variants/{variantId}"
    )
    public ResponseEntity<OutfitItem> addItem(
            @PathVariable Long outfitId,
            @PathVariable Long productId,
            @PathVariable Long variantId) {

        OutfitItem outfitItem =
                outfitItemService
                        .addItem(
                                outfitId,
                                productId,
                                variantId
                        );

        /*
         * addItem 可能是新增，
         * 也可能是替換，
         * 因此統一回傳 200 OK。
         */
        return ResponseEntity
                .ok(outfitItem);
    }


    // ╔══════════════════════════════════╗
    // ║ Add Item With Slot (指定位置)    ║
    // ╚══════════════════════════════════╝

    /**
     * 將商品加入指定穿搭的指定位置。
     *
     * 由前端明確指定 slotType，
     * 因此 TOP 與 OUTER 可以同時存在。
     *
     * 如果該位置已有商品，則直接替換。
     *
     * POST
     * /api/outfit-items/outfits/{outfitId}
     *
     * Request：
     * {
     *     "productId": 10,
     *     "variantId": 12,
     *     "slotType": "TOP"
     * }
     *
     * @param outfitId 穿搭 ID
     * @param request  商品 ID、規格 ID 與穿搭位置
     * @return 新增或替換完成的 OutfitItem
     */
    @PostMapping(
            "/outfits/{outfitId}"
    )
    public ResponseEntity<OutfitItem> addItemWithSlot(
            @PathVariable Long outfitId,
            @Valid @RequestBody OutfitItemSlotRequest request) {

        OutfitItem outfitItem =
                outfitItemService
                        .addItemWithSlot(
                                outfitId,
                                request.getProductId(),
                                request.getVariantId(),
                                request.getSlotType());

        return ResponseEntity
                .ok(outfitItem);
    }


    // ╔══════════════════════╗
    // ║ Find Outfit Items    ║
    // ╚══════════════════════╝

    /**
     * 查詢指定穿搭中的全部商品。
     *
     * GET
     * /api/outfit-items/outfits/{outfitId}
     *
     * @param outfitId 穿搭 ID
     * @return 穿搭商品（含規格顏色/試穿圖）分頁結果
     */
    @GetMapping(
            "/outfits/{outfitId}"
    )
    public ResponseEntity<SelectPartOfData.Result<OutfitItemResponse>> findByOutfitId(
            @PathVariable Long outfitId,
            @RequestParam(defaultValue = "0") int page) {

        SelectPartOfData.Result<OutfitItemResponse> outfitItems =
                outfitItemService
                        .findByOutfitId(outfitId, page);

        return ResponseEntity
                .ok(outfitItems);
    }


    // ╔══════════════════════╗
    // ║ Replace Product      ║
    // ╚══════════════════════╝

    /**
     * 替換指定穿搭位置上的商品。
     *
     * 新商品的位置同樣由
     * Product.categoryType 自動判斷。
     *
     * PUT
     * /api/outfit-items/outfits/{outfitId}/products/{productId}/variants/{variantId}
     *
     * @param outfitId 穿搭 ID
     * @param productId 新商品 ID
     * @param variantId 新規格 ID（顏色）
     * @return 替換完成的 OutfitItem
     */
    @PutMapping(
            "/outfits/{outfitId}/products/{productId}/variants/{variantId}"
    )
    public ResponseEntity<OutfitItem> replaceItem(
            @PathVariable Long outfitId,
            @PathVariable Long productId,
            @PathVariable Long variantId) {

        OutfitItem outfitItem =
                outfitItemService
                        .replaceItem(
                                outfitId,
                                productId,
                                variantId
                        );

        return ResponseEntity
                .ok(outfitItem);
    }


    // ╔══════════════════════╗
    // ║ Remove Item          ║
    // ╚══════════════════════╝

    /**
     * 移除指定 OutfitItem。
     *
     * 對應試衣間右側商品的 X 按鈕。
     *
     * DELETE
     * /api/outfit-items/{outfitItemId}
     *
     * @param outfitItemId OutfitItem ID
     * @return 204 No Content
     */
    @DeleteMapping(
            "/{outfitItemId}"
    )
    public ResponseEntity<Void> removeItem(
            @PathVariable Long outfitItemId) {

        outfitItemService
                .removeItem(outfitItemId);

        return ResponseEntity
                .noContent()
                .build();
    }


    // ╔══════════════════════╗
    // ║ Clear Outfit         ║
    // ╚══════════════════════╝

    /**
     * 清空指定穿搭中的全部商品。
     *
     * 對應試衣間「清空穿搭」按鈕。
     *
     * Outfit 本身仍會保留。
     *
     * DELETE
     * /api/outfit-items/outfits/{outfitId}
     *
     * @param outfitId 穿搭 ID
     * @return 204 No Content
     */
    @DeleteMapping(
            "/outfits/{outfitId}"
    )
    public ResponseEntity<Void> clearOutfit(
            @PathVariable Long outfitId) {

        outfitItemService
                .clearOutfit(outfitId);

        return ResponseEntity
                .noContent()
                .build();
    }
}
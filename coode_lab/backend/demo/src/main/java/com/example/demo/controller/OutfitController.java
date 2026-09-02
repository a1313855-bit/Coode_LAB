package com.example.demo.controller;

// ========== Spring ==========
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// ========== Validation ==========
import jakarta.validation.Valid;

// ========== Java ==========
import java.util.List;

// ========== DTO ==========
import com.example.demo.dto.OutfitCreateRequest;
import com.example.demo.dto.OutfitUpdateRequest;
import com.example.demo.dto.OutfitResponse;
import com.example.demo.dto.OutfitItemResponse;

// ========== Model ==========
import com.example.demo.model.Outfit;
import com.example.demo.model.OutfitItem;

// ========== Service ==========
import com.example.demo.service.OutfitService;
import com.example.demo.service.OutfitItemService;

// ========== Util ==========
import com.example.demo.util.SelectPartOfData;


@RestController
@RequestMapping("/api/outfits")
public class OutfitController {

    // ╔═════════╗
    // ║ Service ║
    // ╚═════════╝

    private final OutfitService outfitService;
    private final OutfitItemService outfitItemService;


    // ==================== 建構子注入 ====================

    public OutfitController(
            OutfitService outfitService,
            OutfitItemService outfitItemService) {

        this.outfitService = outfitService;
        this.outfitItemService = outfitItemService;
    }


    // ==================== 建立穿搭 ====================

    /*
     * POST /api/outfits
     *
     * 前端傳入：
     * {
     *     "userId": 1,
     *     "name": "夏日穿搭"
     * }
     */
    @PostMapping
    public ResponseEntity<OutfitResponse> createOutfit(
            @Valid @RequestBody OutfitCreateRequest request) {

        /*
         * 呼叫 Service 建立穿搭
         */
        Outfit outfit = outfitService.createOutfit(
                request.getUserId(),
                request.getName()
        );

        /*
         * 將 Entity 轉成 Response DTO
         */
        OutfitResponse response = toResponse(outfit);

        /*
         * 建立成功回傳 201 Created
         */
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    // ==================== 查詢單一穿搭 ====================

    /*
     * GET /api/outfits/{outfitId}
     *
     * 例如：
     * GET /api/outfits/1
     */
    @GetMapping("/{outfitId}")
    public ResponseEntity<OutfitResponse> findById(
            @PathVariable Long outfitId) {

        /*
         * 根據 outfitId 查詢穿搭
         */
        Outfit outfit = outfitService.findById(outfitId);

        /*
         * Entity 轉成 Response DTO
         */
        OutfitResponse response = toResponse(outfit);

        /*
         * 查詢成功回傳 200 OK
         */
        return ResponseEntity.ok(response);
    }


    // ==================== 查詢使用者的所有穿搭 ====================

    /*
     * GET /api/outfits/user/{userId}
     *
     * 例如：
     * GET /api/outfits/user/1
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<SelectPartOfData.Result<OutfitResponse>> findByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page) {

        /*
         * 查詢此使用者建立的所有穿搭
         */
        SelectPartOfData.Result<Outfit> outfits =
                outfitService.findByUserId(userId, page);

        /*
         * 將 List<Outfit>
         * 轉成 List<OutfitResponse>
         */
        List<OutfitResponse> response =
                outfits.getContent().stream()
                        .map(this::toResponse)
                        .toList();

        SelectPartOfData.Result<OutfitResponse> result =
                new SelectPartOfData.Result<>(
                        response,
                        outfits.getPage(),
                        outfits.getSize(),
                        outfits.getTotalElements(),
                        outfits.getTotalPages(),
                        outfits.isLast()
                );

        /*
         * 查詢成功回傳 200 OK
         */
        return ResponseEntity.ok(result);
    }


    // ==================== 修改穿搭名稱 ====================

    /*
     * PATCH /api/outfits/{outfitId}
     *
     * 前端傳入：
     * {
     *     "name": "新的穿搭名稱"
     * }
     */
    @PatchMapping("/{outfitId}")
    public ResponseEntity<OutfitResponse> updateOutfitName(
            @PathVariable Long outfitId,
            @Valid @RequestBody OutfitUpdateRequest request) {

        /*
         * 呼叫 Service 修改穿搭名稱
         */
        Outfit outfit =
                outfitService.updateOutfitName(
                        outfitId,
                        request.getName()
                );

        /*
         * Entity 轉成 DTO
         */
        OutfitResponse response = toResponse(outfit);

        /*
         * 修改成功回傳 200 OK
         */
        return ResponseEntity.ok(response);
    }


    // ==================== 刪除穿搭 ====================

    /*
     * DELETE /api/outfits/{outfitId}
     *
     * 例如：
     * DELETE /api/outfits/1
     */
    @DeleteMapping("/{outfitId}")
    public ResponseEntity<Void> deleteOutfit(
            @PathVariable Long outfitId) {

        /*
         * 呼叫 Service 刪除穿搭
         */
        outfitService.deleteOutfit(outfitId);

        /*
         * 刪除成功
         * 回傳 204 No Content
         */
        return ResponseEntity.noContent().build();
    }


    // ==================== Entity → DTO ====================

    /*
     * 將 Outfit Entity
     * 轉換成 OutfitResponse DTO
     */
    private OutfitResponse toResponse(Outfit outfit) {

        /*
         * 根據 outfitId 查詢此穿搭裡面的所有 OutfitItem
         */
        List<OutfitItemResponse> items =
                outfitItemService
                        .findByOutfitId(outfit.getOutfitId(), 0)
                        .getContent()
                        .stream()
                        .map(this::toItemResponse)
                        .toList();

        return new OutfitResponse(
                outfit.getOutfitId(),
                outfit.getUser().getUserId(),
                outfit.getName(),
                items
        );
    }


    /*
     * 將 OutfitItem Entity
     * 轉換成 OutfitItemResponse DTO
     */
    private OutfitItemResponse toItemResponse(
            OutfitItem outfitItem) {

        return new OutfitItemResponse(
                outfitItem.getOutfititemsId(),
                outfitItem.getOutfit().getOutfitId(),
                outfitItem.getProduct().getProductId(),
                outfitItem.getSlotType()
        );
    }
}
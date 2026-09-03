package com.example.demo.controller;

// ========== Spring ==========
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

// ========== Project ==========
import com.example.demo.dto.returnitem.CreateReturnItemRequest;
import com.example.demo.dto.returnitem.UpdateReturnItemQuantityRequest;
import com.example.demo.dto.returnitem.UpdateReturnItemStatusRequest;
import com.example.demo.dto.returnitem.ReturnItemDTO;
import com.example.demo.service.ReturnRequestService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/return-requests/{returnRequestId}/items")
public class ReturnItemController {

    // ╔═══════════════╗
    // ║ Constructor   ║
    // ╚═══════════════╝
    private final ReturnRequestService returnRequestService;

    public ReturnItemController(ReturnRequestService returnRequestService) {
        this.returnRequestService = returnRequestService;
    }

    // ╔══════════════════╗
    // ║ ReturnItem API   ║
    // ╚══════════════════╝

    @PostMapping
    public ResponseEntity<ReturnItemDTO> addReturnItem(
            @PathVariable Long returnRequestId,
            @Valid @RequestBody CreateReturnItemRequest request) {
        ReturnItemDTO returnItem = returnRequestService.addReturnItem(returnRequestId, request);
        if (returnItem != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(returnItem);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<ReturnItemDTO> findItemByReturnRequestId(@PathVariable Long returnRequestId) {
        return returnRequestService.findItemByReturnRequestId(returnRequestId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{returnItemId}/quantity")
    public ResponseEntity<ReturnItemDTO> updateQuantity(
            @PathVariable Long returnItemId,
            @Valid @RequestBody UpdateReturnItemQuantityRequest request) {
        ReturnItemDTO returnItem = returnRequestService.updateQuantity(returnItemId, request);
        if (returnItem != null) {
            return ResponseEntity.ok(returnItem);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{returnItemId}/status")
    public ResponseEntity<ReturnItemDTO> updateStatus(
            @PathVariable Long returnItemId,
            @Valid @RequestBody UpdateReturnItemStatusRequest request) {
        ReturnItemDTO returnItem = returnRequestService.updateStatus(returnItemId, request);
        if (returnItem != null) {
            return ResponseEntity.ok(returnItem);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 廠商審核申請（通過 / 拒絕）
    @PutMapping("/{returnItemId}/review")
    public ResponseEntity<ReturnItemDTO> review(
            @PathVariable Long returnItemId,
            @RequestParam Long vendorId,
            @RequestParam String decision) {
        ReturnItemDTO returnItem = returnRequestService.vendorReview(returnItemId, vendorId, decision);
        return ResponseEntity.ok(returnItem);
    }

    // 廠商推進下一階段
    @PutMapping("/{returnItemId}/advance")
    public ResponseEntity<ReturnItemDTO> vendorAdvance(
            @PathVariable Long returnItemId,
            @RequestParam Long vendorId) {
        ReturnItemDTO returnItem = returnRequestService.advanceVendorStatus(returnItemId, vendorId);
        return ResponseEntity.ok(returnItem);
    }

    // 廠商手動修改狀態（異常修正）
    @PutMapping("/{returnItemId}/vendor-status")
    public ResponseEntity<ReturnItemDTO> vendorStatus(
            @PathVariable Long returnItemId,
            @RequestParam Long vendorId,
            @Valid @RequestBody UpdateReturnItemStatusRequest request) {
        ReturnItemDTO returnItem = returnRequestService.vendorManualStatus(returnItemId, vendorId, request.getStatus());
        return ResponseEntity.ok(returnItem);
    }

    // 會員確認已寄回
    @PutMapping("/{returnItemId}/member-shipped-back")
    public ResponseEntity<ReturnItemDTO> memberShippedBack(
            @PathVariable Long returnItemId,
            @RequestParam Long userId) {
        ReturnItemDTO returnItem = returnRequestService.memberConfirmShippedBack(returnItemId, userId);
        return ResponseEntity.ok(returnItem);
    }

    // 會員確認收到換貨商品
    @PutMapping("/{returnItemId}/member-exchange-received")
    public ResponseEntity<ReturnItemDTO> memberExchangeReceived(
            @PathVariable Long returnItemId,
            @RequestParam Long userId) {
        ReturnItemDTO returnItem = returnRequestService.memberConfirmExchangeReceived(returnItemId, userId);
        return ResponseEntity.ok(returnItem);
    }

    // 管理員手動修改狀態
    @PutMapping("/{returnItemId}/admin-status")
    public ResponseEntity<ReturnItemDTO> adminStatus(
            @PathVariable Long returnItemId,
            @Valid @RequestBody UpdateReturnItemStatusRequest request) {
        ReturnItemDTO returnItem = returnRequestService.adminUpdateStatus(returnItemId, request.getStatus());
        return ResponseEntity.ok(returnItem);
    }
}

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
}

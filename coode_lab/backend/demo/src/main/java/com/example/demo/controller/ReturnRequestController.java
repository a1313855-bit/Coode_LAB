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
import com.example.demo.dto.returnrequest.CreateReturnRequestRequest;
import com.example.demo.dto.returnrequest.UpdateReturnRequestStatusRequest;
import com.example.demo.dto.returnrequest.ReturnRequestDTO;
import com.example.demo.service.ReturnRequestService;
import com.example.demo.util.SelectPartOfData;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/return-requests")
public class ReturnRequestController {

    // ╔═══════════════╗
    // ║ Constructor ║
    // ╚═══════════════╝
    private final ReturnRequestService returnRequestService;

    public ReturnRequestController(ReturnRequestService returnRequestService) {
        this.returnRequestService = returnRequestService;
    }

    // ╔══════════════════════════════╗
    // ║ ReturnRequest API ║
    // ╚══════════════════════════════╝

    @PostMapping("/user/{userId}/order/{orderId}")
    public ResponseEntity<ReturnRequestDTO> createReturnRequest(
            @PathVariable Long userId,
            @PathVariable Long orderId,
            @Valid @RequestBody CreateReturnRequestRequest request) {
        ReturnRequestDTO returnRequest = returnRequestService.createReturnRequest(userId, orderId, request);
        if (returnRequest != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(returnRequest);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReturnRequestDTO> findById(@PathVariable Long id) {
        return returnRequestService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/all")
    public ResponseEntity<SelectPartOfData.Result<ReturnRequestDTO>> getAllReturnRequests(
            @RequestParam(defaultValue = "0") int page) {
        SelectPartOfData.Result<ReturnRequestDTO> list = returnRequestService.findAll(page);
        if (!list.getContent().isEmpty()) {
            return ResponseEntity.ok(list);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<SelectPartOfData.Result<ReturnRequestDTO>> findByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page) {
        SelectPartOfData.Result<ReturnRequestDTO> list = returnRequestService.findByUserId(userId, page);
        if (!list.getContent().isEmpty()) {
            return ResponseEntity.ok(list);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<SelectPartOfData.Result<ReturnRequestDTO>> findByVendorId(
            @PathVariable Long vendorId,
            @RequestParam(defaultValue = "0") int page) {
        SelectPartOfData.Result<ReturnRequestDTO> list = returnRequestService.findByVendorId(vendorId, page);
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ReturnRequestDTO> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReturnRequestStatusRequest request) {
        ReturnRequestDTO returnRequest = returnRequestService.updateStatus(id, request);
        if (returnRequest != null) {
            return ResponseEntity.ok(returnRequest);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 會員取消退換貨申請（尚未進入不可取消階段）
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ReturnRequestDTO> cancel(
            @PathVariable Long id,
            @RequestParam Long userId) {
        ReturnRequestDTO returnRequest = returnRequestService.memberCancel(id, userId);
        return ResponseEntity.ok(returnRequest);
    }
}

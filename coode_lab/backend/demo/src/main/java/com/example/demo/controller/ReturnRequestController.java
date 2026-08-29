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
import com.example.demo.dto.returnrequest.CreateReturnRequestRequest;
import com.example.demo.dto.returnrequest.UpdateReturnRequestStatusRequest;
import com.example.demo.dto.returnrequest.ReturnRequestDTO;
import com.example.demo.model.ReturnRequest;
import com.example.demo.service.ReturnRequestService;

import jakarta.validation.Valid;
import java.util.List;

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
    public ResponseEntity<ReturnRequest> createReturnRequest(
            @PathVariable Long userId,
            @PathVariable Long orderId,
            @Valid @RequestBody CreateReturnRequestRequest request) {
        ReturnRequest returnRequest = returnRequestService.createReturnRequest(userId, orderId, request);
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
    public ResponseEntity<List<ReturnRequestDTO>> getAllReturnRequests() {
        // TODO: 查詢所有申請（admin 管理員）
        List<ReturnRequestDTO> list = returnRequestService.findAll();
        if (!list.isEmpty()) {
            return ResponseEntity.ok(list);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReturnRequestDTO>> findByUserId(@PathVariable Long userId) {
        List<ReturnRequestDTO> list = returnRequestService.findByUserId(userId);
        if (!list.isEmpty()) {
            return ResponseEntity.ok(list);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<ReturnRequestDTO>> findByVendorId(@PathVariable Long vendorId) {
        List<ReturnRequestDTO> list = returnRequestService.findByVendorId(vendorId);
        if (!list.isEmpty()) {
            return ResponseEntity.ok(list);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ReturnRequest> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReturnRequestStatusRequest request) {
        ReturnRequest returnRequest = returnRequestService.updateStatus(id, request);
        if (returnRequest != null) {
            return ResponseEntity.ok(returnRequest);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

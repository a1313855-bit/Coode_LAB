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
import com.example.demo.dto.orderitem.CreateOrderItemRequest;
import com.example.demo.dto.orderitem.UpdateOrderItemRequest;
import com.example.demo.dto.orderitem.UpdateOrderItemStatusRequest;
import com.example.demo.dto.orderitem.VendorOrderItemQuery;
import com.example.demo.dto.orderitem.OrderItemDTO;
import com.example.demo.dto.orderitem.OrderItemVendorDTO;
import com.example.demo.model.OrderItem;
import com.example.demo.service.OrderService;
import com.example.demo.util.SelectPartOfData;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/orders/{orderId}/items")
public class OrderItemController {

    // ╔═══════════════╗
    // ║ Constructor   ║
    // ╚═══════════════╝
    private final OrderService orderService;

    public OrderItemController(OrderService orderService) {
        this.orderService = orderService;
    }

    // ╔════════════╗
    // ║ CRUD API  ║
    // ╚════════════╝

    @PostMapping
    public ResponseEntity<OrderItem> createOrderItem(
            @PathVariable Long orderId,
            @Valid @RequestBody CreateOrderItemRequest request) {
        OrderItem orderItem = orderService.createOrderItem(orderId, request);
        if (orderItem != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(orderItem);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<SelectPartOfData.Result<OrderItemDTO>> getItemsByOrderId(
            @PathVariable Long orderId,
            @RequestParam(defaultValue = "0") int page) {
        SelectPartOfData.Result<OrderItemDTO> items = orderService.findItemsByOrderId(orderId, page);
        if (!items.getContent().isEmpty()) {
            return ResponseEntity.ok(items);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{orderItemId}/status")
    public ResponseEntity<OrderItem> updateStatus(
            @PathVariable Long orderItemId,
            @Valid @RequestBody UpdateOrderItemStatusRequest request) {
        OrderItem orderItem = orderService.updateStatus(orderItemId, request.getStatus());
        if (orderItem != null) {
            return ResponseEntity.ok(orderItem);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{orderItemId}")
    public ResponseEntity<OrderItem> updateOrderItem(
            @PathVariable Long orderItemId,
            @Valid @RequestBody UpdateOrderItemRequest request) {
        OrderItem orderItem = orderService.updateOrderItem(orderItemId, request);
        if (orderItem != null) {
            return ResponseEntity.ok(orderItem);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 廠商推進訂單明細到下一階段（開始處理 / 確認出貨）
    @PutMapping("/{orderItemId}/advance")
    public ResponseEntity<OrderItem> advanceOrderItem(
            @PathVariable Long orderItemId,
            @RequestParam Long vendorId) {
        OrderItem orderItem = orderService.advanceVendorStatus(orderItemId, vendorId);
        return ResponseEntity.ok(orderItem);
    }

    // 會員確認收貨（SHIPPED→RECEIVED）
    @PutMapping("/{orderItemId}/confirm-received")
    public ResponseEntity<OrderItem> confirmReceived(
            @PathVariable Long orderItemId,
            @RequestParam Long userId) {
        OrderItem orderItem = orderService.confirmReceived(orderItemId, userId);
        return ResponseEntity.ok(orderItem);
    }

    // 廠商修改訂單明細狀態（異常修正用，僅限廠商權限內狀態）
    @PutMapping("/{orderItemId}/vendor-status")
    public ResponseEntity<OrderItem> vendorStatus(
            @PathVariable Long orderItemId,
            @RequestParam Long vendorId,
            @Valid @RequestBody UpdateOrderItemStatusRequest request) {
        OrderItem orderItem = orderService.vendorManualStatus(orderItemId, vendorId, request.getStatus());
        return ResponseEntity.ok(orderItem);
    }

    // 管理員修改訂單明細狀態（人工修正用）
    @PutMapping("/{orderItemId}/admin-status")
    public ResponseEntity<OrderItem> adminStatus(
            @PathVariable Long orderItemId,
            @Valid @RequestBody UpdateOrderItemStatusRequest request) {
        OrderItem orderItem = orderService.adminUpdateStatus(orderItemId, request.getStatus());
        return ResponseEntity.ok(orderItem);
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<SelectPartOfData.Result<OrderItemVendorDTO>> getItemsByVendorId(
            @PathVariable Long vendorId,
            @RequestParam(defaultValue = "0") int page,
            @Valid VendorOrderItemQuery query) {
        SelectPartOfData.Result<OrderItemVendorDTO> items;
        if (query.getStatus() == null) {
            items = orderService.findItemsByVendorId(vendorId, page);
        } else {
            items = orderService.findItemsByVendorIdAndStatus(vendorId, query.getStatus(), page);
        }
        return ResponseEntity.ok(items);
    }

}

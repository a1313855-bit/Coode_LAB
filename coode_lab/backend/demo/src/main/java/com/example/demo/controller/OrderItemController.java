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
import com.example.demo.dto.orderitem.CreateOrderItemRequest;
import com.example.demo.dto.orderitem.UpdateOrderItemStatusRequest;
import com.example.demo.dto.orderitem.VendorOrderItemQuery;
import com.example.demo.dto.orderitem.OrderItemDTO;
import com.example.demo.dto.orderitem.OrderItemVendorDTO;
import com.example.demo.model.OrderItem;
import com.example.demo.service.OrderService;

import jakarta.validation.Valid;
import java.util.List;

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
    public ResponseEntity<List<OrderItemDTO>> getItemsByOrderId(@PathVariable Long orderId) {
        List<OrderItemDTO> items = orderService.findItemsByOrderId(orderId);
        if (!items.isEmpty()) {
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

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<OrderItemVendorDTO>> getItemsByVendorId(
            @PathVariable Long vendorId,
            @Valid VendorOrderItemQuery query) {
        List<OrderItemVendorDTO> items;
        if (query.getStatus() == null) {
            items = orderService.findItemsByVendorId(vendorId);
        } else {
            items = orderService.findItemsByVendorIdAndStatus(vendorId, query.getStatus());
        }
        if (!items.isEmpty()) {
            return ResponseEntity.ok(items);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}

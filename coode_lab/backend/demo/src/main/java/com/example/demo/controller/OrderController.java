package com.example.demo.controller;

// ========== Spring ==========
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.demo.dto.order.CreateOrderRequest;
import com.example.demo.dto.order.UpdateRecipientRequest;
import com.example.demo.dto.order.OrderDTO;
import com.example.demo.model.Order;
import com.example.demo.service.OrderService;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/orders")
public class OrderController {

    final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // ╔════════════╗
    // ║ CRUD API ║
    // ╚════════════╝

    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        // TODO: 新增訂單
        Order order = orderService.createOrder(request);
        if (order != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(order);
        } else {
            return ResponseEntity.badRequest().build();
        }

    }

    @GetMapping("/user")
    public ResponseEntity<List<OrderDTO>> getOrdersByUserId(@RequestParam Long userId) {
        // TODO: 查詢訂單列表（依使用者）
        List<OrderDTO> orders = orderService.findByUserId(userId);
        if (!orders.isEmpty()) {
            return ResponseEntity.ok(orders);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderByUserId(@PathVariable Long id) {
        // TODO: 查詢訂單（依ID）
        return orderService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/all")
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        // TODO: 查詢所有訂單（admin 管理員）
        List<OrderDTO> orders = orderService.findAll();
        if (!orders.isEmpty()) {
            return ResponseEntity.ok(orders);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/recipient")
    public ResponseEntity<Order> updateRecipient(@PathVariable Long id, @Valid @RequestBody UpdateRecipientRequest request) {
        Order order = orderService.updateRecipient(id, request);
        if (order != null) {
            return ResponseEntity.ok(order);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // TODO: 刪除訂單

}

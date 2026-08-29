package com.example.demo.service;

// ========== Java ==========
import java.util.List;
import java.util.Optional;

// ========== Project ==========
import com.example.demo.model.Order;
import com.example.demo.model.OrderItem;
import com.example.demo.dto.order.CreateOrderRequest;
import com.example.demo.dto.order.UpdateRecipientRequest;
import com.example.demo.dto.orderitem.CreateOrderItemRequest;
import com.example.demo.dto.order.OrderDTO;
import com.example.demo.dto.orderitem.OrderItemDTO;
import com.example.demo.dto.orderitem.OrderItemVendorDTO;

public interface OrderService {

    // ╔══════════════════════════════╗
    // ║ Order（訂單主表）            ║
    // ╚══════════════════════════════╝

    // 將選中的 CartItem 結帳並建立訂單
    Order createOrder(CreateOrderRequest request);

    // 查詢會員的所有訂單
    List<OrderDTO> findByUserId(Long userId);

    // 查詢單一訂單
    Optional<OrderDTO> findById(Long orderId);

    // 查詢所有訂單（admin 管理員系統）
    List<OrderDTO> findAll();

    // 修改收件資訊
    Order updateRecipient(Long orderId, UpdateRecipientRequest request);

    // ╔══════════════════════════════╗
    // ║ OrderItem（訂單明細）        ║
    // ╚══════════════════════════════╝

    // 建立訂單明細
    OrderItem createOrderItem(Long orderId, CreateOrderItemRequest request);

    // 查詢某張訂單所有商品
    List<OrderItemDTO> findItemsByOrderId(Long orderId);

    // 廠商查看自己收到的訂單商品
    List<OrderItemVendorDTO> findItemsByVendorId(Long vendorId);

    // 廠商查看自己收到的訂單商品（依狀態群組）
    List<OrderItemVendorDTO> findItemsByVendorIdAndStatus(Long vendorId, String vendorStatus);

    // 修改訂單明細狀態
    OrderItem updateStatus(Long orderItemId, String status);
}

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
import com.example.demo.util.SelectPartOfData;

public interface OrderService {

    // ╔══════════════════════════════╗
    // ║ Order（訂單主表）            ║
    // ╚══════════════════════════════╝

    // 將選中的 CartItem 結帳並建立訂單（回傳 DTO，避免 Entity 序列化無限迴圈）
    OrderDTO createOrder(CreateOrderRequest request);

    // 查詢會員的所有訂單 (固定每頁10筆,page 從 0 開始)
    SelectPartOfData.Result<OrderDTO> findByUserId(Long userId, int page);

    // 查詢單一訂單
    Optional<OrderDTO> findById(Long orderId);

    // 查詢所有訂單（admin 管理員系統）(固定每頁10筆,page 從 0 開始)
    SelectPartOfData.Result<OrderDTO> findAll(int page);

    // 查詢所有訂單 + 關鍵字搜尋（訂單ID / 會員名稱 / Email）(固定每頁10筆)
    SelectPartOfData.Result<OrderDTO> findAllSearch(String keyword, int page);

    // 修改收件資訊（回傳 DTO，避免 Entity 序列化無限迴圈）
    OrderDTO updateRecipient(Long orderId, UpdateRecipientRequest request);

    // ╔══════════════════════════════╗
    // ║ OrderItem（訂單明細）        ║
    // ╚══════════════════════════════╝

    // 建立訂單明細
    OrderItem createOrderItem(Long orderId, CreateOrderItemRequest request);

    // 查詢某張訂單所有商品 (固定每頁10筆,page 從 0 開始)
    SelectPartOfData.Result<OrderItemDTO> findItemsByOrderId(Long orderId, int page);

    // 廠商查看自己收到的訂單商品 (固定每頁10筆,page 從 0 開始)
    SelectPartOfData.Result<OrderItemVendorDTO> findItemsByVendorId(Long vendorId, int page);

    // 廠商查看自己收到的訂單商品（依狀態群組）(固定每頁10筆,page 從 0 開始)
    SelectPartOfData.Result<OrderItemVendorDTO> findItemsByVendorIdAndStatus(Long vendorId, String vendorStatus, int page);

    // 修改訂單明細狀態
    OrderItem updateStatus(Long orderItemId, String status);

    // 廠商推進訂單明細到下一階段（開始處理 / 確認出貨）。
    // 僅限該明細所屬廠商操作；PENDING→PROCESSING→SHIPPED。
    // 廠商不可自行將 SHIPPED 改成 RECEIVED（必須由會員確認收貨）。
    OrderItem advanceVendorStatus(Long orderItemId, Long vendorId);

    // 會員確認收貨：SHIPPED→RECEIVED。僅限該訂單所屬會員操作。
    OrderItem confirmReceived(Long orderItemId, Long userId);

    // 廠商修改訂單明細狀態（異常/誤按修正用）。
    // 下拉只能送出廠商有權限的狀態，且不可送出會員才能設定的 RECEIVED。
    OrderItem vendorManualStatus(Long orderItemId, Long vendorId, String status);

    // 管理員修改訂單明細狀態（人工修正用）。
    OrderItem adminUpdateStatus(Long orderItemId, String status);

    // 修改訂單明細內容（數量 / 價格 / 狀態）
    OrderItem updateOrderItem(Long orderItemId, com.example.demo.dto.orderitem.UpdateOrderItemRequest request);
}

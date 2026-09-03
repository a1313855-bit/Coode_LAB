package com.example.demo.service;

// ========== Java ==========
import java.util.List;
import java.util.Optional;

// ========== Project ==========
import com.example.demo.dto.returnrequest.CreateReturnRequestRequest;
import com.example.demo.dto.returnrequest.UpdateReturnRequestStatusRequest;
import com.example.demo.dto.returnrequest.ReturnRequestDTO;
import com.example.demo.dto.returnitem.CreateReturnItemRequest;
import com.example.demo.dto.returnitem.UpdateReturnItemQuantityRequest;
import com.example.demo.dto.returnitem.UpdateReturnItemStatusRequest;
import com.example.demo.dto.returnitem.ReturnItemDTO;
import com.example.demo.util.SelectPartOfData;

public interface ReturnRequestService {

    // ╔══════════════════════════════════╗
    // ║ ReturnRequest（退換貨申請主表）  ║
    // ╚══════════════════════════════════╝

    // 建立退貨或換貨申請
    ReturnRequestDTO createReturnRequest(Long userId, Long orderId, CreateReturnRequestRequest request);

    // 查詢單一申請
    Optional<ReturnRequestDTO> findById(Long returnRequestId);

    // 查詢所有申請（admin 管理員系統）(固定每頁10筆,page 從 0 開始)
    SelectPartOfData.Result<ReturnRequestDTO> findAll(int page);

    // 查詢會員所有申請 (固定每頁10筆,page 從 0 開始)
    SelectPartOfData.Result<ReturnRequestDTO> findByUserId(Long userId, int page);

    // 查詢某廠商需要處理的申請 (固定每頁10筆,page 從 0 開始)
    SelectPartOfData.Result<ReturnRequestDTO> findByVendorId(Long vendorId, int page);

    // 更新申請狀態
    ReturnRequestDTO updateStatus(Long returnRequestId, UpdateReturnRequestStatusRequest request);

    // ╔══════════════════════════════════╗
    // ║ ReturnItem（退換貨商品明細）    ║
    // ╚══════════════════════════════════╝

    // 新增退換貨商品明細
    ReturnItemDTO addReturnItem(Long returnRequestId, CreateReturnItemRequest request);

    // 一對一後，查詢申請對應的單一 ReturnItem
    Optional<ReturnItemDTO> findItemByReturnRequestId(Long returnRequestId);

    // 設定核准/拒絕數量
    ReturnItemDTO updateQuantity(Long returnItemId, UpdateReturnItemQuantityRequest request);

    // 更新退換貨明細狀態（線性推進），廠商下決定時一併將申請單設為 REVIEWED
    ReturnItemDTO updateStatus(Long returnItemId, UpdateReturnItemStatusRequest request);

    // ╔══════════════════════════════════╗
    // ║ 退換貨狀態流程（角色權限驗證）    ║
    // ╚══════════════════════════════════╝

    // 廠商審核申請：PENDING_REVIEW → APPROVED / REJECTED
    ReturnItemDTO vendorReview(Long returnItemId, Long vendorId, String decision);

    // 廠商推進下一階段：SHIPPED_BACK→RECEIVED（確認收件）、
    // RECEIVED(RETURN)→REFUNDING→REFUNDED、RECEIVED(EXCHANGE)→EXCHANGING→EXCHANGE_SHIPPED
    ReturnItemDTO advanceVendorStatus(Long returnItemId, Long vendorId);

    // 廠商手動修改狀態（異常修正，僅限廠商權限內狀態）
    ReturnItemDTO vendorManualStatus(Long returnItemId, Long vendorId, String status);

    // 會員確認已寄回：AWAITING_SHIPBACK→SHIPPED_BACK
    ReturnItemDTO memberConfirmShippedBack(Long returnItemId, Long userId);

    // 會員確認收到換貨商品：EXCHANGE_SHIPPED→EXCHANGED
    ReturnItemDTO memberConfirmExchangeReceived(Long returnItemId, Long userId);

    // 會員取消申請（若尚未進入不可取消階段）
    ReturnRequestDTO memberCancel(Long returnRequestId, Long userId);

    // 管理員手動修改狀態（人工修正）
    ReturnItemDTO adminUpdateStatus(Long returnItemId, String status);

    // ╔══════════════════════════════════╗
    // ║ 訂單退換貨資格（供訂單頁面使用）║
    // ╚══════════════════════════════════╝

    // 此訂單是否已有「進行中」的退換貨申請（未走到終態）
    boolean hasInProgressReturnForOrder(Long orderId);

    // 此訂單目前的退換貨狀態：PROCESSING（進行中）/ COMPLETED（已完成）/ null（無申請）
    String returnStatusForOrder(Long orderId);
}

package com.example.demo.service;

// ========== Java ==========
import java.util.List;
import java.util.Optional;

// ========== Project ==========
import com.example.demo.model.ReturnRequest;
import com.example.demo.model.ReturnItem;
import com.example.demo.dto.returnrequest.CreateReturnRequestRequest;
import com.example.demo.dto.returnrequest.UpdateReturnRequestStatusRequest;
import com.example.demo.dto.returnrequest.ReturnRequestDTO;
import com.example.demo.dto.returnitem.CreateReturnItemRequest;
import com.example.demo.dto.returnitem.UpdateReturnItemQuantityRequest;
import com.example.demo.dto.returnitem.UpdateReturnItemStatusRequest;
import com.example.demo.dto.returnitem.ReturnItemDTO;

public interface ReturnRequestService {

    // ╔══════════════════════════════════╗
    // ║ ReturnRequest（退換貨申請主表）  ║
    // ╚══════════════════════════════════╝

    // 建立退貨或換貨申請
    ReturnRequest createReturnRequest(Long userId, Long orderId, CreateReturnRequestRequest request);

    // 查詢單一申請
    Optional<ReturnRequestDTO> findById(Long returnRequestId);

    // 查詢所有申請（admin 管理員系統）
    List<ReturnRequestDTO> findAll();

    // 查詢會員所有申請
    List<ReturnRequestDTO> findByUserId(Long userId);

    // 查詢某廠商需要處理的申請
    List<ReturnRequestDTO> findByVendorId(Long vendorId);

    // 更新申請狀態
    ReturnRequest updateStatus(Long returnRequestId, UpdateReturnRequestStatusRequest request);

    // ╔══════════════════════════════════╗
    // ║ ReturnItem（退換貨商品明細）    ║
    // ╚══════════════════════════════════╝

    // 新增退換貨商品明細
    ReturnItem addReturnItem(Long returnRequestId, CreateReturnItemRequest request);

    // 一對一後，查詢申請對應的單一 ReturnItem
    Optional<ReturnItemDTO> findItemByReturnRequestId(Long returnRequestId);

    // 設定核准/拒絕數量
    ReturnItem updateQuantity(Long returnItemId, UpdateReturnItemQuantityRequest request);

    // 更新退換貨明細狀態（線性推進），廠商下決定時一併將申請單設為 REVIEWED
    ReturnItem updateStatus(Long returnItemId, UpdateReturnItemStatusRequest request);
}

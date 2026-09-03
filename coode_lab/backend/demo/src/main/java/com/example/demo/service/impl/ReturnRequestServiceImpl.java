package com.example.demo.service.impl;

// ========== Spring ==========
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// ========== Java ==========
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

// ========== Project ==========
import com.example.demo.model.ReturnRequest;
import com.example.demo.model.ReturnItem;
import com.example.demo.model.Order;
import com.example.demo.model.OrderItem;
import com.example.demo.model.User;
import com.example.demo.model.Vendor;
import com.example.demo.model.Product;
import com.example.demo.model.ProductVariant;
import com.example.demo.dto.returnrequest.CreateReturnRequestRequest;
import com.example.demo.dto.returnrequest.UpdateReturnRequestStatusRequest;
import com.example.demo.dto.returnrequest.ReturnRequestDTO;
import com.example.demo.dto.returnitem.CreateReturnItemRequest;
import com.example.demo.dto.returnitem.UpdateReturnItemQuantityRequest;
import com.example.demo.dto.returnitem.UpdateReturnItemStatusRequest;
import com.example.demo.dto.returnitem.ReturnItemDTO;
import com.example.demo.repository.ReturnRequestRepository;
import com.example.demo.repository.ReturnItemRepository;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VendorRepository;
import com.example.demo.service.ReturnRequestService;
import com.example.demo.util.SelectPartOfData;

@Service
@Transactional
public class ReturnRequestServiceImpl implements ReturnRequestService {

    // ╔═══════════════╗
    // ║ Constructor ║
    // ╚═══════════════╝
    private final ReturnRequestRepository returnRequestRepository;
    private final ReturnItemRepository returnItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;

    public ReturnRequestServiceImpl(ReturnRequestRepository returnRequestRepository,
            ReturnItemRepository returnItemRepository,
            OrderItemRepository orderItemRepository,
            OrderRepository orderRepository,
            UserRepository userRepository,
            VendorRepository vendorRepository) {
        this.returnRequestRepository = returnRequestRepository;
        this.returnItemRepository = returnItemRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.vendorRepository = vendorRepository;
    }

    // ╔══════════════════════════════════╗
    // ║ ReturnRequest（退換貨申請主表） ║
    // ╚══════════════════════════════════╝

    @Override
    @Transactional
    public ReturnRequestDTO createReturnRequest(Long userId, Long orderId, CreateReturnRequestRequest request) {
        Optional<Order> optional = orderRepository.findById(orderId);
        if (optional.isEmpty()) {
            throw new IllegalArgumentException("找不到訂單 ID:" + orderId);
        }
        Order order = optional.get();

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("找不到會員 ID:" + userId);
        }

        // 只有訂單擁有者可以申請退換貨
        if (order.getUser() == null || !order.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("此訂單不屬於此會員");
        }

        // 該訂單明細必須存在，且必須屬於此訂單
        Optional<OrderItem> itemOptional = orderItemRepository.findById(request.getOrderItemId());
        if (itemOptional.isEmpty()) {
            throw new IllegalArgumentException("找不到訂單明細 ID:" + request.getOrderItemId());
        }
        OrderItem orderItem = itemOptional.get();
        if (orderItem.getOrder() == null || !orderItem.getOrder().getOrderId().equals(orderId)) {
            throw new IllegalArgumentException("訂單明細不存在於此訂單");
        }

        // 核心商業規則：只有「訂單明細已完成」才有資格申請退換貨，
        // 且同一筆訂單不可重複申請（已有進行中的退換貨申請即阻擋）。
        // 此處同時涵蓋前端顯示與後端防護，避免繞過前端直接呼叫 API。
        if ("CANCELLED".equals(orderItem.getStatus())) {
            throw new IllegalArgumentException("訂單已取消，無法申請退換貨");
        }
        if (!"RECEIVED".equals(orderItem.getStatus())) {
            throw new IllegalArgumentException("訂單尚未完成，目前無法申請退換貨");
        }
        List<ReturnRequest> existingRequests = returnRequestRepository.findByOrder_OrderId(orderId);
        if (hasInProgressReturn(existingRequests)) {
            throw new IllegalArgumentException("此訂單已有退換貨申請正在處理中");
        }

        // 退換貨數量不可大於該明細的購買數量
        Integer orderQuantity = orderItem.getProductQuantity();
        if (orderQuantity == null || orderQuantity < 1) {
            throw new IllegalArgumentException("此商品數量無法申請退換貨");
        }
        if (request.getRequestQuantity() > orderQuantity) {
            throw new IllegalArgumentException("退換貨數量不可大於訂單明細數量");
        }

        Vendor vendor = orderItem.getVendor();
        if (vendor == null) {
            throw new IllegalArgumentException("此訂單明細無法識別所屬廠商");
        }

        ReturnRequest returnRequest = new ReturnRequest();
        returnRequest.setStatus("PENDING");
        returnRequest.setRequestType(request.getRequestType());
        returnRequest.setReturnRequestQuantity(request.getRequestQuantity());
        returnRequest.setOrder(order);
        returnRequest.setUser(user);
        returnRequest.setVendor(vendor);

        // 一併建立退換貨明細（包含退貨照片）
        ReturnItem returnItem = new ReturnItem();
        returnItem.setStatus("PENDING_REVIEW");
        returnItem.setApprovalQuantity(request.getRequestQuantity());
        returnItem.setRejectedQuantity(0);
        returnItem.setRefund(BigDecimal.ZERO);
        returnItem.setPicture(request.getPicture());
        returnItem.setReason(request.getReason());
        returnItem.setOrderItem(orderItem);
        returnItem.setReturnRequest(returnRequest);
        returnRequest.setReturnItem(returnItem);

        ReturnRequest saved = returnRequestRepository.save(returnRequest);
        return toReturnRequestDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReturnRequestDTO> findById(Long returnRequestId) {
        return returnRequestRepository.findById(returnRequestId).map(this::toReturnRequestDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public SelectPartOfData.Result<ReturnRequestDTO> findAll(int page) {
        List<ReturnRequestDTO> all = returnRequestRepository.findAll().stream()
                .map(this::toReturnRequestDTO)
                .toList();
        return SelectPartOfData.pageOf10(all, page);
    }

    @Override
    @Transactional(readOnly = true)
    public SelectPartOfData.Result<ReturnRequestDTO> findByUserId(Long userId, int page) {
        List<ReturnRequestDTO> all = returnRequestRepository.findByUser_UserId(userId).stream()
                .map(this::toReturnRequestDTO)
                .toList();
        return SelectPartOfData.pageOf10(all, page);
    }

    @Override
    @Transactional(readOnly = true)
    public SelectPartOfData.Result<ReturnRequestDTO> findByVendorId(Long vendorId, int page) {
        List<ReturnRequestDTO> all = returnRequestRepository.findByVendor_VendorId(vendorId).stream()
                .map(this::toReturnRequestDTO)
                .toList();
        return SelectPartOfData.pageOf10(all, page);
    }

    @Override
    public ReturnRequestDTO updateStatus(Long returnRequestId, UpdateReturnRequestStatusRequest request) {
        Optional<ReturnRequest> optional = returnRequestRepository.findById(returnRequestId);
        if (optional.isEmpty()) {
            return null;
        }
        ReturnRequest returnRequest = optional.get();
        returnRequest.setStatus(request.getStatus());
        return toReturnRequestDTO(returnRequestRepository.save(returnRequest));
    }

    // ╔══════════════════════════════════╗
    // ║ ReturnRequest 通用方法（申請主表）║
    // ╚══════════════════════════════════╝

    private ReturnRequestDTO toReturnRequestDTO(ReturnRequest returnRequest) {
        ReturnRequestDTO dto = new ReturnRequestDTO();
        dto.setReturnRequestsId(returnRequest.getReturnRequestsId());
        dto.setStatus(returnRequest.getStatus());
        dto.setRequestType(returnRequest.getRequestType());
        dto.setReturnRequestQuantity(returnRequest.getReturnRequestQuantity());
        dto.setCreatedAt(returnRequest.getCreatedAt());

        if (returnRequest.getOrder() != null) {
            ReturnRequestDTO.OrderInfo orderInfo = new ReturnRequestDTO.OrderInfo();
            orderInfo.setOrderId(returnRequest.getOrder().getOrderId());
            dto.setOrder(orderInfo);
        }
        if (returnRequest.getVendor() != null) {
            ReturnRequestDTO.VendorInfo vendorInfo = new ReturnRequestDTO.VendorInfo();
            vendorInfo.setVendorId(returnRequest.getVendor().getVendorId());
            vendorInfo.setVendorName(returnRequest.getVendor().getVendorName());
            dto.setVendor(vendorInfo);
        }
        if (returnRequest.getUser() != null) {
            ReturnRequestDTO.UserInfo userInfo = new ReturnRequestDTO.UserInfo();
            userInfo.setUserId(returnRequest.getUser().getUserId());
            userInfo.setName(returnRequest.getUser().getName());
            dto.setUser(userInfo);
        }
        if (returnRequest.getReturnItem() != null) {
            ReturnItem returnItem = returnRequest.getReturnItem();
            ReturnRequestDTO.ReturnItemInfo itemInfo = new ReturnRequestDTO.ReturnItemInfo();
            itemInfo.setReturnItemId(returnItem.getReturnItemId());
            itemInfo.setStatus(returnItem.getStatus());
            itemInfo.setApprovalQuantity(returnItem.getApprovalQuantity());
            itemInfo.setRejectedQuantity(returnItem.getRejectedQuantity());
            itemInfo.setPicture(returnItem.getPicture());
            if (returnItem.getOrderItem() != null) {
                itemInfo.setOrderItemId(returnItem.getOrderItem().getOrderItemId());
                ProductVariant variant = returnItem.getOrderItem().getVariant();
                if (variant != null) {
                    Product product = variant.getProduct();
                    itemInfo.setProductName(product.getName());
                    itemInfo.setCategoryType(product.getCategoryType());
                    itemInfo.setColor(variant.getColor());
                    itemInfo.setSize(variant.getSize());
                    itemInfo.setPattern(product.getPattern());
                }
            }
            dto.setReturnItem(itemInfo);
        }
        return dto;
    }

    // ╔══════════════════════════════════╗
    // ║ ReturnItem（退換貨商品明細） ║
    // ╚══════════════════════════════════╝

    @Override
    public ReturnItemDTO addReturnItem(Long returnRequestId, CreateReturnItemRequest request) {
        Optional<ReturnRequest> requestOptional = returnRequestRepository.findById(returnRequestId);
        if (requestOptional.isEmpty()) {
            return null;
        }
        ReturnRequest returnRequest = requestOptional.get();

        Optional<OrderItem> itemOptional = orderItemRepository.findById(request.getOrderItemId());
        if (itemOptional.isEmpty()) {
            return null;
        }
        OrderItem orderItem = itemOptional.get();

        ReturnItem returnItem = new ReturnItem();
        returnItem.setStatus("PENDING_REVIEW");
        returnItem.setApprovalQuantity(request.getRequestQuantity());
        returnItem.setRejectedQuantity(0);
        returnItem.setRefund(BigDecimal.ZERO);
        returnItem.setPicture(request.getPicture());
        returnItem.setOrderItem(orderItem);
        returnItem.setReturnRequest(returnRequest);

        return toReturnItemDTO(returnItemRepository.save(returnItem));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReturnItemDTO> findItemByReturnRequestId(Long returnRequestId) {
        return returnItemRepository.findByReturnRequest_ReturnRequestsId(returnRequestId).stream()
                .findFirst()
                .map(this::toReturnItemDTO);
    }

    @Override
    public ReturnItemDTO updateQuantity(Long returnItemId, UpdateReturnItemQuantityRequest request) {
        Optional<ReturnItem> optional = returnItemRepository.findById(returnItemId);
        if (optional.isEmpty()) {
            return null;
        }
        ReturnItem returnItem = optional.get();

        Integer approvedQuantity = request.getApprovedQuantity();
        Integer rejectedQuantity = request.getRejectedQuantity();

        String requestType = returnItem.getReturnRequest().getRequestType();
        OrderItem orderItem = returnItem.getOrderItem();

        if ("EXCHANGE".equals(requestType)) {
            returnItem.setApprovalQuantity(approvedQuantity);
            returnItem.setRejectedQuantity(0);
            returnItem.setRefund(BigDecimal.ZERO);
        } else {
            returnItem.setApprovalQuantity(approvedQuantity);
            returnItem.setRejectedQuantity(0);
            returnItem.setRefund(orderItem.getPrice()
                    .multiply(BigDecimal.valueOf(approvedQuantity)));
        }

        returnItem.setRejectedQuantity(rejectedQuantity);
        returnItem.setStatus("PROCESSING");

        return toReturnItemDTO(returnItemRepository.save(returnItem));
    }

    // 廠商下決定時，一併將申請單狀態設為 REVIEWED（方案 C）
    @Override
    public ReturnItemDTO updateStatus(Long returnItemId, UpdateReturnItemStatusRequest request) {
        Optional<ReturnItem> optional = returnItemRepository.findById(returnItemId);
        if (optional.isEmpty()) {
            return null;
        }
        ReturnItem returnItem = optional.get();
        returnItem.setStatus(request.getStatus());

        ReturnRequest returnRequest = returnItem.getReturnRequest();
        returnRequest.setStatus("REVIEWED");

        return toReturnItemDTO(returnItemRepository.save(returnItem));
    }

    // ╔══════════════════════════════════╗
    // ║ 退換貨狀態流程（角色權限驗證）    ║
    // ╚══════════════════════════════════╝

    // 規範狀態常數
    private static final String R_PENDING_REVIEW = "PENDING_REVIEW";
    private static final String R_APPROVED = "APPROVED";
    private static final String R_REJECTED = "REJECTED";
    private static final String R_AWAITING_SHIPBACK = "AWAITING_SHIPBACK";
    private static final String R_SHIPPED_BACK = "SHIPPED_BACK";
    private static final String R_RECEIVED = "RECEIVED";
    private static final String R_REFUNDING = "REFUNDING";
    private static final String R_REFUNDED = "REFUNDED";
    private static final String R_EXCHANGING = "EXCHANGING";
    private static final String R_EXCHANGE_SHIPPED = "EXCHANGE_SHIPPED";
    private static final String R_EXCHANGED = "EXCHANGED";
    private static final String R_COMPLETED = "COMPLETED";
    private static final String R_CANCELLED = "CANCELLED";
    private static final String TYPE_RETURN = "RETURN";
    private static final String TYPE_EXCHANGE = "EXCHANGE";

    // 廠商審核申請：PENDING_REVIEW → APPROVED / REJECTED
    @Override
    @Transactional
    public ReturnItemDTO vendorReview(Long returnItemId, Long vendorId, String decision) {
        ReturnItem item = returnItemRepository.findById(returnItemId)
                .orElseThrow(() -> new IllegalArgumentException("查無此退換貨明細"));
        // 權限：僅限該申請對應廠商
        if (item.getReturnRequest() == null
                || item.getReturnRequest().getVendor() == null
                || !item.getReturnRequest().getVendor().getVendorId().equals(vendorId)) {
            throw new IllegalArgumentException("僅能審核自己廠商的退換貨申請");
        }
        String cur = item.getStatus();
        if (!R_PENDING_REVIEW.equals(cur)) {
            throw new IllegalArgumentException("此申請尚未處於待審核狀態");
        }
        if (R_APPROVED.equals(decision)) {
            item.setStatus(R_APPROVED);
        } else if (R_REJECTED.equals(decision)) {
            item.setStatus(R_REJECTED);
        } else {
            throw new IllegalArgumentException("審核決定只能是 通過（APPROVED）或 拒絕（REJECTED）");
        }
        item.getReturnRequest().setStatus("REVIEWED");
        return toReturnItemDTO(returnItemRepository.save(item));
    }

    // 廠商推進下一階段（依退/換貨與目前狀態）
    @Override
    @Transactional
    public ReturnItemDTO advanceVendorStatus(Long returnItemId, Long vendorId) {
        ReturnItem item = returnItemRepository.findById(returnItemId)
                .orElseThrow(() -> new IllegalArgumentException("查無此退換貨明細"));
        if (item.getReturnRequest() == null
                || item.getReturnRequest().getVendor() == null
                || !item.getReturnRequest().getVendor().getVendorId().equals(vendorId)) {
            throw new IllegalArgumentException("僅能操作自己廠商的退換貨申請");
        }
        String cur = item.getStatus();
        String type = item.getReturnRequest().getRequestType();
        String next;

        if (R_SHIPPED_BACK.equals(cur)) {
            next = R_RECEIVED;                       // 確認收件
        } else if (R_RECEIVED.equals(cur)) {
            if (TYPE_RETURN.equals(type)) {
                next = R_REFUNDING;                  // 退貨：開始退款
            } else {
                next = R_EXCHANGING;                 // 換貨：開始換貨
            }
        } else if (R_REFUNDING.equals(cur)) {
            next = R_REFUNDED;                       // 退貨：完成退款
        } else if (R_EXCHANGING.equals(cur)) {
            next = R_EXCHANGE_SHIPPED;               // 換貨：確認換貨商品出貨
        } else if (R_APPROVED.equals(cur)) {
            next = R_AWAITING_SHIPBACK;              // 審核通過後等待會員寄回
        } else {
            throw new IllegalArgumentException("目前狀態無法由廠商推進");
        }
        item.setStatus(next);
        return toReturnItemDTO(returnItemRepository.save(item));
    }

    // 廠商手動修改狀態（異常修正，僅限廠商權限內狀態）
    @Override
    @Transactional
    public ReturnItemDTO vendorManualStatus(Long returnItemId, Long vendorId, String status) {
        ReturnItem item = returnItemRepository.findById(returnItemId)
                .orElseThrow(() -> new IllegalArgumentException("查無此退換貨明細"));
        if (item.getReturnRequest() == null
                || item.getReturnRequest().getVendor() == null
                || !item.getReturnRequest().getVendor().getVendorId().equals(vendorId)) {
            throw new IllegalArgumentException("僅能操作自己廠商的退換貨申請");
        }
        String type = item.getReturnRequest().getRequestType();
        if (!isVendorAllowedStatus(status, type)) {
            throw new IllegalArgumentException("非廠商可設定的狀態");
        }
        item.setStatus(status);
        return toReturnItemDTO(returnItemRepository.save(item));
    }

    // 會員確認已寄回：AWAITING_SHIPBACK→SHIPPED_BACK
    @Override
    @Transactional
    public ReturnItemDTO memberConfirmShippedBack(Long returnItemId, Long userId) {
        ReturnItem item = returnItemRepository.findById(returnItemId)
                .orElseThrow(() -> new IllegalArgumentException("查無此退換貨明細"));
        if (item.getReturnRequest() == null
                || item.getReturnRequest().getUser() == null
                || !item.getReturnRequest().getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("僅能操作自己的退換貨申請");
        }
        if (!R_AWAITING_SHIPBACK.equals(item.getStatus())) {
            throw new IllegalArgumentException("目前狀態無法確認已寄回");
        }
        item.setStatus(R_SHIPPED_BACK);
        return toReturnItemDTO(returnItemRepository.save(item));
    }

    // 會員確認收到換貨商品：EXCHANGE_SHIPPED→EXCHANGED
    @Override
    @Transactional
    public ReturnItemDTO memberConfirmExchangeReceived(Long returnItemId, Long userId) {
        ReturnItem item = returnItemRepository.findById(returnItemId)
                .orElseThrow(() -> new IllegalArgumentException("查無此退換貨明細"));
        if (item.getReturnRequest() == null
                || item.getReturnRequest().getUser() == null
                || !item.getReturnRequest().getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("僅能操作自己的退換貨申請");
        }
        if (!R_EXCHANGE_SHIPPED.equals(item.getStatus())) {
            throw new IllegalArgumentException("唯有換貨商品已出貨才能確認收到");
        }
        item.setStatus(R_EXCHANGED);
        return toReturnItemDTO(returnItemRepository.save(item));
    }

    // 會員取消申請（若尚未進入不可取消階段）
    @Override
    @Transactional
    public ReturnRequestDTO memberCancel(Long returnRequestId, Long userId) {
        ReturnRequest req = returnRequestRepository.findById(returnRequestId)
                .orElseThrow(() -> new IllegalArgumentException("查無此退換貨申請"));
        if (req.getUser() == null || !req.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("僅能取消自己的退換貨申請");
        }
        if (req.getReturnItem() != null) {
            String s = req.getReturnItem().getStatus();
            // 已進入不可取消階段（已寄回/收件後）
            if (!R_PENDING_REVIEW.equals(s) && !R_APPROVED.equals(s) && !R_AWAITING_SHIPBACK.equals(s)) {
                throw new IllegalArgumentException("此申請已進入不可取消階段");
            }
            req.getReturnItem().setStatus(R_CANCELLED);
        }
        req.setStatus(R_CANCELLED);
        return toReturnRequestDTO(returnRequestRepository.save(req));
    }

    // 管理員手動修改狀態（人工修正）
    @Override
    @Transactional
    public ReturnItemDTO adminUpdateStatus(Long returnItemId, String status) {
        ReturnItem item = returnItemRepository.findById(returnItemId)
                .orElseThrow(() -> new IllegalArgumentException("查無此退換貨明細"));
        if (!isValidReturnStatus(status)) {
            throw new IllegalArgumentException("非有效的退換貨狀態");
        }
        item.setStatus(status);
        return toReturnItemDTO(returnItemRepository.save(item));
    }

    // 此訂單是否已有「進行中」的退換貨申請（供訂單頁面判斷能否再次申請）
    @Override
    @Transactional(readOnly = true)
    public boolean hasInProgressReturnForOrder(Long orderId) {
        List<ReturnRequest> requests = returnRequestRepository.findByOrder_OrderId(orderId);
        return hasInProgressReturn(requests);
    }

    // 此訂單目前的退換貨狀態（供訂單頁面顯示「退換貨處理中 / 已完成」）
    @Override
    @Transactional(readOnly = true)
    public String returnStatusForOrder(Long orderId) {
        List<ReturnRequest> requests = returnRequestRepository.findByOrder_OrderId(orderId);
        return currentReturnStatus(requests);
    }

    // 此筆訂單是否已有「進行中」的退換貨申請（尚未走到終態，則不可再次申請）
    private boolean hasInProgressReturn(List<ReturnRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return false;
        }
        for (ReturnRequest req : requests) {
            if (req.getReturnItem() != null) {
                String s = req.getReturnItem().getStatus();
                if (!isReturnTerminalStatus(s)) {
                    return true;
                }
            }
        }
        return false;
    }

    // 判斷某退換貨明細狀態是否為「終態」（流程已結束，不再阻擋再次申請）
    private boolean isReturnTerminalStatus(String status) {
        return R_REFUNDED.equals(status)
                || R_EXCHANGED.equals(status)
                || R_COMPLETED.equals(status)
                || R_CANCELLED.equals(status)
                || R_REJECTED.equals(status);
    }

    // 此筆訂單目前的退換貨狀態（供前端顯示）：
    // 有進行中申請 → PROCESSING；已完成申請（曾走到終態）→ COMPLETED；無 → null
    private String currentReturnStatus(List<ReturnRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return null;
        }
        boolean hasCompleted = false;
        for (ReturnRequest req : requests) {
            if (req.getReturnItem() == null) {
                continue;
            }
            String s = req.getReturnItem().getStatus();
            if (isReturnTerminalStatus(s)) {
                hasCompleted = true;
            } else {
                return "PROCESSING";
            }
        }
        return hasCompleted ? "COMPLETED" : null;
    }

    // 廠商可設定的狀態（依退/換貨）
    private boolean isVendorAllowedStatus(String status, String type) {
        if (R_PENDING_REVIEW.equals(status)
                || R_APPROVED.equals(status) || R_REJECTED.equals(status)
                || R_AWAITING_SHIPBACK.equals(status)
                || R_SHIPPED_BACK.equals(status)
                || R_RECEIVED.equals(status)) {
            return true;
        }
        if (TYPE_RETURN.equals(type)) {
            return R_REFUNDING.equals(status) || R_REFUNDED.equals(status);
        }
        if (TYPE_EXCHANGE.equals(type)) {
            return R_EXCHANGING.equals(status)
                    || R_EXCHANGE_SHIPPED.equals(status)
                    || R_EXCHANGED.equals(status);
        }
        return false;
    }

    // 退換貨合法狀態總集合（管理員用）
    private boolean isValidReturnStatus(String status) {
        return R_PENDING_REVIEW.equals(status)
                || R_APPROVED.equals(status) || R_REJECTED.equals(status)
                || R_AWAITING_SHIPBACK.equals(status)
                || R_SHIPPED_BACK.equals(status)
                || R_RECEIVED.equals(status)
                || R_REFUNDING.equals(status)
                || R_REFUNDED.equals(status)
                || R_EXCHANGING.equals(status)
                || R_EXCHANGE_SHIPPED.equals(status)
                || R_EXCHANGED.equals(status)
                || R_COMPLETED.equals(status)
                || R_CANCELLED.equals(status);
    }

    // ╔══════════════════════════════════╗
    // ║ ReturnItem 通用方法（退換貨明細）║
    // ╚══════════════════════════════════╝

    private ReturnItemDTO toReturnItemDTO(ReturnItem returnItem) {
        ReturnItemDTO dto = new ReturnItemDTO();
        dto.setReturnItemId(returnItem.getReturnItemId());
        dto.setStatus(returnItem.getStatus());
        dto.setReason(returnItem.getReason());
        dto.setDescription(returnItem.getDescription());
        dto.setPicture(returnItem.getPicture());
        dto.setApprovalQuantity(returnItem.getApprovalQuantity());
        dto.setRejectedQuantity(returnItem.getRejectedQuantity());
        dto.setRefund(returnItem.getRefund());

        if (returnItem.getReturnRequest() != null) {
            ReturnItemDTO.ReturnRequestInfo returnRequestInfo = new ReturnItemDTO.ReturnRequestInfo();
            returnRequestInfo.setReturnRequestsId(returnItem.getReturnRequest().getReturnRequestsId());
            returnRequestInfo.setRequestType(returnItem.getReturnRequest().getRequestType());
            returnRequestInfo.setReturnRequestQuantity(returnItem.getReturnRequest().getReturnRequestQuantity());
            dto.setReturnRequest(returnRequestInfo);
        }
        if (returnItem.getOrderItem() != null) {
            dto.setOrderItem(toReturnItemOrderItemInfo(returnItem.getOrderItem()));
        }
        return dto;
    }

    private ReturnItemDTO.OrderItemInfo toReturnItemOrderItemInfo(OrderItem orderItem) {
        ReturnItemDTO.OrderItemInfo orderItemInfo = new ReturnItemDTO.OrderItemInfo();
        orderItemInfo.setOrderItemId(orderItem.getOrderItemId());
        orderItemInfo.setProductQuantity(orderItem.getProductQuantity());
        orderItemInfo.setPrice(orderItem.getPrice());

        if (orderItem.getVariant() != null) {
            ProductVariant variant = orderItem.getVariant();
            Product product = variant.getProduct();
            ReturnItemDTO.ProductInfo productInfo = new ReturnItemDTO.ProductInfo();
            productInfo.setProductId(product.getProductId());
            productInfo.setName(product.getName());
            productInfo.setPattern(product.getPattern());
            productInfo.setCategoryType(product.getCategoryType());
            productInfo.setStyle(product.getStyle());
            productInfo.setColor(variant.getColor());
            productInfo.setSize(variant.getSize());
            orderItemInfo.setProduct(productInfo);
        }
        return orderItemInfo;
    }

}
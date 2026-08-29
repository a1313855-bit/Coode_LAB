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
import com.example.demo.service.ReturnRequestService;

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

    public ReturnRequestServiceImpl(ReturnRequestRepository returnRequestRepository,
            ReturnItemRepository returnItemRepository,
            OrderItemRepository orderItemRepository,
            OrderRepository orderRepository) {
        this.returnRequestRepository = returnRequestRepository;
        this.returnItemRepository = returnItemRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderRepository = orderRepository;
    }

    // ╔══════════════════════════════════╗
    // ║ ReturnRequest（退換貨申請主表） ║
    // ╚══════════════════════════════════╝

    // TODO: 依賴 UserRepository / VendorRepository（其他組員負責），待整合後改為真實查詢
    // 目前 User 與 Vendor 以假資料代替（標註處需替換）
    @Override
    public ReturnRequest createReturnRequest(Long userId, Long orderId, CreateReturnRequestRequest request) {
        Optional<Order> optional = orderRepository.findById(orderId);
        if (optional.isEmpty()) {
            return null;
        }
        Order order = optional.get();

        // TODO: 替換成 userRepository.findById(userId)
        User user = new User();
        user.setUserId(userId);

        // TODO: 依商品明細所屬廠商取得真實 Vendor（待整合後改為真實查詢）
        Vendor vendor = new Vendor();
        vendor.setVendorId(1L);

        ReturnRequest returnRequest = new ReturnRequest();
        returnRequest.setStatus("PENDING");
        returnRequest.setRequestType(request.getRequestType());
        returnRequest.setReturnRequestQuantity(request.getReturnRequestQuantity());
        returnRequest.setOrder(order);
        returnRequest.setUser(user);
        returnRequest.setVendor(vendor);

        return returnRequestRepository.save(returnRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReturnRequestDTO> findById(Long returnRequestId) {
        return returnRequestRepository.findById(returnRequestId).map(this::toReturnRequestDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnRequestDTO> findAll() {
        return returnRequestRepository.findAll().stream()
                .map(this::toReturnRequestDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnRequestDTO> findByUserId(Long userId) {
        return returnRequestRepository.findByUser_UserId(userId).stream()
                .map(this::toReturnRequestDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnRequestDTO> findByVendorId(Long vendorId) {
        return returnRequestRepository.findByVendor_VendorId(vendorId).stream()
                .map(this::toReturnRequestDTO)
                .toList();
    }

    @Override
    public ReturnRequest updateStatus(Long returnRequestId, UpdateReturnRequestStatusRequest request) {
        Optional<ReturnRequest> optional = returnRequestRepository.findById(returnRequestId);
        if (optional.isEmpty()) {
            return null;
        }
        ReturnRequest returnRequest = optional.get();
        returnRequest.setStatus(request.getStatus());
        return returnRequestRepository.save(returnRequest);
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
        return dto;
    }

    // ╔══════════════════════════════════╗
    // ║ ReturnItem（退換貨商品明細） ║
    // ╚══════════════════════════════════╝

    @Override
    public ReturnItem addReturnItem(Long returnRequestId, CreateReturnItemRequest request) {
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
        returnItem.setReturnedQuantity(request.getRequestQuantity());
        returnItem.setExchangedQuantity(0);
        returnItem.setRejectedQuantity(0);
        returnItem.setRefund(BigDecimal.ZERO);
        returnItem.setOrderItem(orderItem);
        returnItem.setReturnRequest(returnRequest);

        return returnItemRepository.save(returnItem);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReturnItemDTO> findItemByReturnRequestId(Long returnRequestId) {
        return returnItemRepository.findByReturnRequest_ReturnRequestsId(returnRequestId).stream()
                .findFirst()
                .map(this::toReturnItemDTO);
    }

    @Override
    public ReturnItem updateQuantity(Long returnItemId, UpdateReturnItemQuantityRequest request) {
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
            returnItem.setExchangedQuantity(approvedQuantity);
            returnItem.setReturnedQuantity(0);
            returnItem.setRefund(BigDecimal.ZERO);
        } else {
            returnItem.setReturnedQuantity(approvedQuantity);
            returnItem.setExchangedQuantity(0);
            returnItem.setRefund(orderItem.getPrice()
                    .multiply(BigDecimal.valueOf(approvedQuantity)));
        }

        returnItem.setRejectedQuantity(rejectedQuantity);
        returnItem.setStatus("PROCESSING");

        return returnItemRepository.save(returnItem);
    }

    // 廠商下決定時，一併將申請單狀態設為 REVIEWED（方案 C）
    @Override
    public ReturnItem updateStatus(Long returnItemId, UpdateReturnItemStatusRequest request) {
        Optional<ReturnItem> optional = returnItemRepository.findById(returnItemId);
        if (optional.isEmpty()) {
            return null;
        }
        ReturnItem returnItem = optional.get();
        returnItem.setStatus(request.getStatus());

        ReturnRequest returnRequest = returnItem.getReturnRequest();
        returnRequest.setStatus("REVIEWED");

        return returnItemRepository.save(returnItem);
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
        dto.setReturnedQuantity(returnItem.getReturnedQuantity());
        dto.setExchangedQuantity(returnItem.getExchangedQuantity());
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

        if (orderItem.getProduct() != null) {
            Product product = orderItem.getProduct();
            ReturnItemDTO.ProductInfo productInfo = new ReturnItemDTO.ProductInfo();
            productInfo.setProductId(product.getProductId());
            productInfo.setName(product.getName());
            productInfo.setPattern(product.getPattern());
            productInfo.setCategoryType(product.getCategoryType());
            productInfo.setStyle(product.getStyle());
            productInfo.setColor(product.getColor());
            productInfo.setSize(product.getSize());
            orderItemInfo.setProduct(productInfo);
        }
        return orderItemInfo;
    }

}
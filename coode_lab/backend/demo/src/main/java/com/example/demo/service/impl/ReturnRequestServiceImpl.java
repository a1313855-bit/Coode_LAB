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
    public ReturnRequest createReturnRequest(Long userId, Long orderId, CreateReturnRequestRequest request) {
        Optional<Order> optional = orderRepository.findById(orderId);
        if (optional.isEmpty()) {
            return null;
        }
        Order order = optional.get();

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }

        // 只有訂單擁有者可以申請退換貨
        if (order.getUser() == null || !order.getUser().getUserId().equals(userId)) {
            return null;
        }

        // 該訂單明細必須存在，且必須屬於此訂單
        Optional<OrderItem> itemOptional = orderItemRepository.findById(request.getOrderItemId());
        if (itemOptional.isEmpty()) {
            return null;
        }
        OrderItem orderItem = itemOptional.get();
        if (orderItem.getOrder() == null || !orderItem.getOrder().getOrderId().equals(orderId)) {
            throw new IllegalArgumentException("訂單明細不存在於此訂單");
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
            return null;
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
        returnItem.setOrderItem(orderItem);
        returnItem.setReturnRequest(returnRequest);
        returnRequest.setReturnItem(returnItem);

        return returnRequestRepository.save(returnRequest);
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
                if (returnItem.getOrderItem().getProduct() != null) {
                    Product product = returnItem.getOrderItem().getProduct();
                    itemInfo.setProductName(product.getName());
                    itemInfo.setCategoryType(product.getCategoryType());
                    itemInfo.setColor(product.getColor());
                    itemInfo.setSize(product.getSize());
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
        returnItem.setApprovalQuantity(request.getRequestQuantity());
        returnItem.setRejectedQuantity(0);
        returnItem.setRefund(BigDecimal.ZERO);
        returnItem.setPicture(request.getPicture());
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
package com.example.demo.service.impl;

// ========== Spring ==========
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

// ========== Java ==========
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

// ========== Project ==========
import com.example.demo.model.Order;
import com.example.demo.model.OrderItem;
import com.example.demo.model.User;
import com.example.demo.model.Product;
import com.example.demo.model.Vendor;
import com.example.demo.model.CartItem;
import com.example.demo.dto.order.CreateOrderRequest;
import com.example.demo.dto.order.UpdateRecipientRequest;
import com.example.demo.dto.orderitem.CreateOrderItemRequest;
import com.example.demo.dto.order.OrderDTO;
import com.example.demo.dto.orderitem.OrderItemDTO;
import com.example.demo.dto.orderitem.OrderItemVendorDTO;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.service.OrderService;
import com.example.demo.util.VendorStatusMapper;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    // ╔═══════════════╗
    // ║ Constructor ║
    // ╚═══════════════╝
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderServiceImpl(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    // ╔══════════════════════════════╗
    // ║ Order（訂單主表） ║
    // ╚══════════════════════════════╝

    // TODO: 依賴 CartItemRepository / UserRepository（其他組員負責），待整合後改為真實查詢
    // 目前 User 與 CartItem 以假資料代替（標註處需替換）
    @Override
    public Order createOrder(CreateOrderRequest request) {
        // TODO: 替換成 userRepository.findById(request.getUserId())
        User user = new User();
        user.setUserId(request.getUserId());

        // TODO: 替換成 cartItemRepository.findAllById(request.getCartItemIds())
        List<CartItem> cartItems = List.of(
                buildPlaceholderCartItem(2L, 2, "500.00"),
                buildPlaceholderCartItem(3L, 2, "1200.00"));

        int totalAmount = 0;
        BigDecimal sumTotal = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            totalAmount += item.getProductQuantity();
            sumTotal = sumTotal.add(item.getPrice()
                    .multiply(BigDecimal.valueOf(item.getProductQuantity())));
        }

        Order order = new Order();
        order.setRecipientName(request.getRecipientName());
        order.setRecipientPhone(request.getRecipientPhone());
        order.setRecipientAddress(request.getRecipientAddress());
        order.setTotalAmount(totalAmount);
        order.setSumTotal(sumTotal);
        order.setUser(user);

        return orderRepository.save(order);
    }

    // TODO: 假資料用輔助方法，整合 CartItemRepository 後可刪除
    private CartItem buildPlaceholderCartItem(Long productId, int quantity, String price) {
        CartItem item = new CartItem();
        Product product = new Product();
        product.setProductId(productId);
        item.setProduct(product);
        item.setProductQuantity(quantity);
        item.setPrice(new BigDecimal(price));
        return item;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> findByUserId(Long userId) {
        List<OrderDTO> data = orderRepository.findByUser_UserId(userId).stream()
                .map(this::toOrderDTO)
                .toList();
        return data;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> findAll() {
        List<OrderDTO> data = orderRepository.findAll().stream()
                .map(this::toOrderDTO)
                .toList();
        return data;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrderDTO> findById(Long orderId) {
        return orderRepository.findById(orderId).map(this::toOrderDTO);
    }

    @Override
    public Order updateRecipient(Long orderId, UpdateRecipientRequest request) {
        Optional<Order> optional = orderRepository.findById(orderId);
        if (optional.isEmpty()) {
            return null;
        }
        Order order = optional.get();
        order.setRecipientName(request.getRecipientName());
        order.setRecipientPhone(request.getRecipientPhone());
        order.setRecipientAddress(request.getRecipientAddress());
        return orderRepository.save(order);
    }

    // ╔══════════════════════════════╗
    // ║ Order 通用方法（訂單主表） ║
    // ╚══════════════════════════════╝

    private OrderDTO toOrderDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setOrderId(order.getOrderId());
        dto.setRecipientName(order.getRecipientName());
        dto.setRecipientPhone(order.getRecipientPhone());
        dto.setRecipientAddress(order.getRecipientAddress());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setSumTotal(order.getSumTotal());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        User user = order.getUser();
        if (user != null) {
            OrderDTO.UserInfo userInfo = new OrderDTO.UserInfo();
            userInfo.setUserId(user.getUserId());
            userInfo.setName(user.getName());
            dto.setUser(userInfo);
        }
        return dto;
    }

    private OrderItemDTO toOrderItemDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setOrderItemId(item.getOrderItemId());
        dto.setProductQuantity(item.getProductQuantity());
        dto.setPrice(item.getPrice());
        dto.setPriceTotal(item.getPriceTotal());
        dto.setStatus(item.getStatus());

        if (item.getOrder() != null) {
            OrderItemDTO.OrderInfo orderInfo = new OrderItemDTO.OrderInfo();
            orderInfo.setOrderId(item.getOrder().getOrderId());
            dto.setOrder(orderInfo);
        }
        if (item.getVendor() != null) {
            OrderItemDTO.VendorInfo vendorInfo = new OrderItemDTO.VendorInfo();
            vendorInfo.setVendorId(item.getVendor().getVendorId());
            vendorInfo.setVendorName(item.getVendor().getVendorName());
            dto.setVendor(vendorInfo);
        }
        if (item.getProduct() != null) {
            dto.setProduct(toOrderItemProductInfo(item.getProduct()));
        }
        return dto;
    }

    private OrderItemVendorDTO toOrderItemVendorDTO(OrderItem item) {
        OrderItemVendorDTO dto = new OrderItemVendorDTO();
        dto.setOrderItemId(item.getOrderItemId());
        dto.setProductQuantity(item.getProductQuantity());
        dto.setPrice(item.getPrice());
        dto.setPriceTotal(item.getPriceTotal());
        dto.setStatus(item.getStatus());

        if (item.getOrder() != null) {
            OrderItemVendorDTO.OrderInfo orderInfo = new OrderItemVendorDTO.OrderInfo();
            orderInfo.setOrderId(item.getOrder().getOrderId());
            orderInfo.setRecipientName(item.getOrder().getRecipientName());
            orderInfo.setRecipientPhone(item.getOrder().getRecipientPhone());
            orderInfo.setRecipientAddress(item.getOrder().getRecipientAddress());
            dto.setOrder(orderInfo);
        }
        if (item.getVendor() != null) {
            OrderItemVendorDTO.VendorInfo vendorInfo = new OrderItemVendorDTO.VendorInfo();
            vendorInfo.setVendorId(item.getVendor().getVendorId());
            vendorInfo.setVendorName(item.getVendor().getVendorName());
            dto.setVendor(vendorInfo);
        }
        if (item.getProduct() != null) {
            OrderItemVendorDTO.ProductInfo productInfo = new OrderItemVendorDTO.ProductInfo();
            productInfo.setProductId(item.getProduct().getProductId());
            productInfo.setName(item.getProduct().getName());
            productInfo.setPattern(item.getProduct().getPattern());
            productInfo.setCategoryType(item.getProduct().getCategoryType());
            productInfo.setStyle(item.getProduct().getStyle());
            productInfo.setColor(item.getProduct().getColor());
            productInfo.setSize(item.getProduct().getSize());
            productInfo.setPrice(item.getProduct().getPrice());
            dto.setProduct(productInfo);
        }
        return dto;
    }

    private OrderItemDTO.ProductInfo toOrderItemProductInfo(Product product) {
        OrderItemDTO.ProductInfo productInfo = new OrderItemDTO.ProductInfo();
        productInfo.setProductId(product.getProductId());
        productInfo.setName(product.getName());
        productInfo.setPattern(product.getPattern());
        productInfo.setCategoryType(product.getCategoryType());
        productInfo.setStyle(product.getStyle());
        productInfo.setColor(product.getColor());
        productInfo.setSize(product.getSize());
        productInfo.setPrice(product.getPrice());
        return productInfo;
    }

    // ╔══════════════════════════════╗
    // ║ OrderItem（訂單明細） ║
    // ╚══════════════════════════════╝

    // TODO: 依賴 ProductRepository / VendorRepository（其他組員負責），待整合後改為真實查詢
    // 目前 Product 與 Vendor 以假資料代替（標註處需替換）
    @Override
    public OrderItem createOrderItem(Long orderId, CreateOrderItemRequest request) {
        Optional<Order> optional = orderRepository.findById(orderId);
        if (optional.isEmpty()) {
            return null;
        }
        Order order = optional.get();

        // TODO: 替換成 productRepository.findById(request.getProductId())
        Product product = new Product();
        product.setProductId(request.getProductId());
        product.setPrice(new BigDecimal("500.00"));

        // TODO: 替換成 vendorRepository.findById(product.getVendor().getVendorId())
        Vendor vendor = new Vendor();
        vendor.setVendorId(1L);

        BigDecimal priceTotal = product.getPrice()
                .multiply(BigDecimal.valueOf(request.getProductQuantity()));

        OrderItem orderItem = new OrderItem();
        orderItem.setProductQuantity(request.getProductQuantity());
        orderItem.setPrice(product.getPrice());
        orderItem.setPriceTotal(priceTotal);
        orderItem.setStatus("PENDING");
        orderItem.setOrder(order);
        orderItem.setVendor(vendor);
        orderItem.setProduct(product);

        return orderItemRepository.save(orderItem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItemDTO> findItemsByOrderId(Long orderId) {
        return orderItemRepository.findByOrder_OrderId(orderId).stream()
                .map(this::toOrderItemDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItemVendorDTO> findItemsByVendorId(Long vendorId) {
        return orderItemRepository.findByVendor_VendorId(vendorId).stream()
                .map(this::toOrderItemVendorDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItemVendorDTO> findItemsByVendorIdAndStatus(Long vendorId, String vendorStatus) {
        List<String> statuses = VendorStatusMapper.toOrderItemStatuses(vendorStatus);
        return orderItemRepository.findByVendor_VendorIdAndStatusIn(vendorId, statuses).stream()
                .map(this::toOrderItemVendorDTO)
                .toList();
    }

    @Override
    public OrderItem updateStatus(Long orderItemId, String status) {
        Optional<OrderItem> optional = orderItemRepository.findById(orderItemId);
        if (optional.isEmpty()) {
            return null;
        }
        OrderItem orderItem = optional.get();
        orderItem.setStatus(status);
        return orderItemRepository.save(orderItem);
    }

}
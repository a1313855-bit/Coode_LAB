package com.example.demo.service.impl;

// ========== Spring ==========
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

// ========== Java ==========
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

// ========== Project ==========
import com.example.demo.model.Cart;
import com.example.demo.model.Order;
import com.example.demo.model.OrderItem;
import com.example.demo.model.User;
import com.example.demo.model.Product;
import com.example.demo.model.ProductVariant;
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
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.CartItemRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.ProductVariantRepository;
import com.example.demo.repository.VendorRepository;
import com.example.demo.service.OrderService;
import com.example.demo.util.VendorStatusMapper;
import com.example.demo.util.SelectPartOfData;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    // ╔═══════════════╗
    // ║ Constructor ║
    // ╚═══════════════╝
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final VendorRepository vendorRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            UserRepository userRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            ProductVariantRepository productVariantRepository,
            VendorRepository vendorRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
        this.vendorRepository = vendorRepository;
    }

    // ╔══════════════════════════════╗
    // ║ Order（訂單主表） ║
    // ╚══════════════════════════════╝

    @Override
    public OrderDTO createOrder(CreateOrderRequest request) {
        // 1. 找會員
        User user = userRepository.findById(request.getUserId()).orElse(null);
        if (user == null) {
            return null;
        }

        // 2. 找出前端選購要結帳的購物車商品
        List<CartItem> cartItems = cartItemRepository.findAllById(request.getCartItemIds());
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("請先選擇要結帳的商品");
        }

        // 3. 確認這些購物車商品都屬於同一個會員（防止越權）
        for (CartItem cartItem : cartItems) {
            if (cartItem.getCart() == null
                    || cartItem.getCart().getUser() == null
                    || !cartItem.getCart().getUser().getUserId().equals(user.getUserId())) {
                throw new IllegalArgumentException("購物車內容與會員不符，請重新整理購物車");
            }
        }

        // 4. 先建立訂單主表
        Order order = new Order();
        order.setRecipientName(request.getRecipientName());
        order.setRecipientPhone(request.getRecipientPhone());
        order.setRecipientAddress(request.getRecipientAddress());
        order.setTotalAmount(0);
        order.setSumTotal(BigDecimal.ZERO);
        order.setUser(user);
        Order savedOrder = orderRepository.save(order);

        // 5. 每個購物車商品：即時檢查庫存 → 扣庫存 → 建立訂單明細
        List<OrderItem> orderItems = new ArrayList<>();
        int totalAmount = 0;
        BigDecimal sumTotal = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            ProductVariant variant = cartItem.getVariant();
            Product product = variant.getProduct();
            int qty = cartItem.getProductQuantity();

            // 雙層狀態校驗：商品 ACTIVE 且 規格 ACTIVE 才能購買
            if (product == null || !"ACTIVE".equals(product.getStatus())) {
                throw new IllegalArgumentException("商品「" + product.getName() + "」目前已停售");
            }
            if (!"ACTIVE".equals(variant.getStatus())) {
                throw new IllegalArgumentException(
                        "商品「" + product.getName() + "」的「" + variant.getColor() + " / " + variant.getSize()
                                + "」規格已停售，無法結帳");
            }

            // 即時庫存檢查（與購物車加入/修改相同邏輯）
            if (variant.getStock() == null || variant.getStock() <= 0) {
                throw new IllegalArgumentException(
                        "商品「" + product.getName() + "」的「" + variant.getColor() + " / " + variant.getSize()
                                + "」目前無庫存");
            }
            if (qty > variant.getStock()) {
                throw new IllegalArgumentException(
                        "商品「" + product.getName() + "」的「" + variant.getColor() + " / " + variant.getSize()
                                + "」庫存不足，目前僅剩 " + variant.getStock() + " 件");
            }

            // 更新規格庫存（扣掉購買數量）
            variant.setStock(variant.getStock() - qty);
            productVariantRepository.save(variant);

            // 建立訂單明細
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setVariant(variant);
            orderItem.setVendor(product.getVendor());
            orderItem.setProductQuantity(qty);
            orderItem.setPrice(product.getPrice());
            orderItem.setPriceTotal(
                    product.getPrice().multiply(BigDecimal.valueOf(qty)));
            orderItem.setStatus("PENDING");
            orderItems.add(orderItem);

            totalAmount += qty;
            sumTotal = sumTotal.add(orderItem.getPriceTotal());
        }

        orderItemRepository.saveAll(orderItems);

        // 6. 更新訂單總額
        savedOrder.setTotalAmount(totalAmount);
        savedOrder.setSumTotal(sumTotal);
        savedOrder = orderRepository.save(savedOrder);

        // 7. 刪除已結帳的購物車商品，並同步 Cart.totalQuantity
        cartItemRepository.deleteAll(cartItems);

        Set<Cart> carts = new LinkedHashSet<>();
        for (CartItem cartItem : cartItems) {
            if (cartItem.getCart() != null) {
                carts.add(cartItem.getCart());
            }
        }
        for (Cart cart : carts) {
            Long count = cartItemRepository.countByCart_CartId(cart.getCartId());
            cart.setTotalQuantity(count.intValue());
        }

        // 回傳 OrderDTO，避免 Entity 序列化無限迴圈
        return toOrderDTO(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public SelectPartOfData.Result<OrderDTO> findByUserId(Long userId, int page) {
        List<OrderDTO> all = orderRepository.findByUser_UserId(userId).stream()
                .map(this::toOrderDTO)
                .toList();
        return SelectPartOfData.pageOf10(all, page);
    }

    @Override
    @Transactional(readOnly = true)
    public SelectPartOfData.Result<OrderDTO> findAll(int page) {
        List<OrderDTO> all = orderRepository.findAll().stream()
                .map(this::toOrderDTO)
                .toList();
        return SelectPartOfData.pageOf10(all, page);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrderDTO> findById(Long orderId) {
        return orderRepository.findById(orderId).map(this::toOrderDTO);
    }

    @Override
    public OrderDTO updateRecipient(Long orderId, UpdateRecipientRequest request) {
        Optional<Order> optional = orderRepository.findById(orderId);
        if (optional.isEmpty()) {
            return null;
        }
        Order order = optional.get();
        order.setRecipientName(request.getRecipientName());
        order.setRecipientPhone(request.getRecipientPhone());
        order.setRecipientAddress(request.getRecipientAddress());
        // 回傳 OrderDTO，避免 Entity 序列化無限迴圈
        return toOrderDTO(orderRepository.save(order));
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
        if (item.getVariant() != null) {
            dto.setVariant(toOrderItemVariantInfo(item.getVariant()));
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
        if (item.getVariant() != null) {
            dto.setVariant(toOrderItemVariantInfoVendor(item.getVariant()));
        }
        return dto;
    }

    private OrderItemDTO.VariantInfo toOrderItemVariantInfo(ProductVariant variant) {
        OrderItemDTO.VariantInfo info = new OrderItemDTO.VariantInfo();
        info.setVariantId(variant.getVariantId());
        info.setColor(variant.getColor());
        info.setSize(variant.getSize());
        info.setStock(variant.getStock());
        info.setStatus(variant.getStatus());
        info.setImagesJpg(variant.getImagesJpg());
        if (variant.getProduct() != null) {
            info.setProduct(toOrderItemProductInfo(variant.getProduct()));
        }
        return info;
    }

    private OrderItemVendorDTO.VariantInfo toOrderItemVariantInfoVendor(ProductVariant variant) {
        OrderItemVendorDTO.VariantInfo info = new OrderItemVendorDTO.VariantInfo();
        info.setVariantId(variant.getVariantId());
        info.setColor(variant.getColor());
        info.setSize(variant.getSize());
        info.setStock(variant.getStock());
        info.setStatus(variant.getStatus());
        info.setImagesJpg(variant.getImagesJpg());
        if (variant.getProduct() != null) {
            OrderItemVendorDTO.ProductInfo productInfo = new OrderItemVendorDTO.ProductInfo();
            Product p = variant.getProduct();
            productInfo.setProductId(p.getProductId());
            productInfo.setName(p.getName());
            productInfo.setPattern(p.getPattern());
            productInfo.setCategoryType(p.getCategoryType());
            productInfo.setStyle(p.getStyle());
            productInfo.setPrice(p.getPrice());
            info.setProduct(productInfo);
        }
        return info;
    }

    private OrderItemDTO.ProductInfo toOrderItemProductInfo(Product product) {
        OrderItemDTO.ProductInfo productInfo = new OrderItemDTO.ProductInfo();
        productInfo.setProductId(product.getProductId());
        productInfo.setName(product.getName());
        productInfo.setPattern(product.getPattern());
        productInfo.setCategoryType(product.getCategoryType());
        productInfo.setStyle(product.getStyle());
        productInfo.setPrice(product.getPrice());
        return productInfo;
    }

    // ╔══════════════════════════════╗
    // ║ OrderItem（訂單明細） ║
    // ╚══════════════════════════════╝

    @Override
    public OrderItem createOrderItem(Long orderId, CreateOrderItemRequest request) {
        Optional<Order> optional = orderRepository.findById(orderId);
        if (optional.isEmpty()) {
            return null;
        }
        Order order = optional.get();

        ProductVariant variant = productVariantRepository.findById(request.getVariantId()).orElse(null);
        if (variant == null) {
            return null;
        }
        Product product = variant.getProduct();

        Vendor vendor = vendorRepository.findById(product.getVendor().getVendorId()).orElse(null);
        if (vendor == null) {
            return null;
        }

        BigDecimal priceTotal = product.getPrice()
                .multiply(BigDecimal.valueOf(request.getProductQuantity()));

        OrderItem orderItem = new OrderItem();
        orderItem.setProductQuantity(request.getProductQuantity());
        orderItem.setPrice(product.getPrice());
        orderItem.setPriceTotal(priceTotal);
        orderItem.setStatus("PENDING");
        orderItem.setOrder(order);
        orderItem.setVendor(vendor);
        orderItem.setVariant(variant);

        return orderItemRepository.save(orderItem);
    }

    @Override
    @Transactional(readOnly = true)
    public SelectPartOfData.Result<OrderItemDTO> findItemsByOrderId(Long orderId, int page) {
        List<OrderItemDTO> all = orderItemRepository.findByOrder_OrderId(orderId).stream()
                .map(this::toOrderItemDTO)
                .toList();
        return SelectPartOfData.pageOf10(all, page);
    }

    @Override
    @Transactional(readOnly = true)
    public SelectPartOfData.Result<OrderItemVendorDTO> findItemsByVendorId(Long vendorId, int page) {
        List<OrderItemVendorDTO> all = orderItemRepository.findByVendor_VendorId(vendorId).stream()
                .map(this::toOrderItemVendorDTO)
                .toList();
        return SelectPartOfData.pageOf10(all, page);
    }

    @Override
    @Transactional(readOnly = true)
    public SelectPartOfData.Result<OrderItemVendorDTO> findItemsByVendorIdAndStatus(Long vendorId, String vendorStatus, int page) {
        List<String> statuses = VendorStatusMapper.toOrderItemStatuses(vendorStatus);
        List<OrderItemVendorDTO> all = orderItemRepository.findByVendor_VendorIdAndStatusIn(vendorId, statuses).stream()
                .map(this::toOrderItemVendorDTO)
                .toList();
        return SelectPartOfData.pageOf10(all, page);
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
package com.example.demo.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.model.Admin;
import com.example.demo.model.Cart;
import com.example.demo.model.CartItem;
import com.example.demo.model.Order;
import com.example.demo.model.OrderItem;
import com.example.demo.model.Outfit;
import com.example.demo.model.OutfitItem;
import com.example.demo.model.Product;
import com.example.demo.model.ReturnItem;
import com.example.demo.model.ReturnRequest;
import com.example.demo.model.User;
import com.example.demo.model.Vendor;
import com.example.demo.repository.AdminRepository;
import com.example.demo.repository.CartItemRepository;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.OutfitItemRepository;
import com.example.demo.repository.OutfitRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.ReturnItemRepository;
import com.example.demo.repository.ReturnRequestRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VendorRepository;

@Component
public class TestFakeData implements CommandLineRunner {

    private final PasswordEncoder passwordEncoder;

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OutfitRepository outfitRepository;
    private final OutfitItemRepository outfitItemRepository;
    private final ReturnRequestRepository returnRequestRepository;
    private final ReturnItemRepository returnItemRepository;

    public TestFakeData(PasswordEncoder passwordEncoder,
                        AdminRepository adminRepository,
                        UserRepository userRepository,
                        VendorRepository vendorRepository,
                        ProductRepository productRepository,
                        CartRepository cartRepository,
                        CartItemRepository cartItemRepository,
                        OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        OutfitRepository outfitRepository,
                        OutfitItemRepository outfitItemRepository,
                        ReturnRequestRepository returnRequestRepository,
                        ReturnItemRepository returnItemRepository) {
        this.passwordEncoder = passwordEncoder;
        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
        this.vendorRepository = vendorRepository;
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.outfitRepository = outfitRepository;
        this.outfitItemRepository = outfitItemRepository;
        this.returnRequestRepository = returnRequestRepository;
        this.returnItemRepository = returnItemRepository;
    }

    @Override
    public void run(String... args) {
        // 若已有資料則跳過，避免重複塞入
        if (adminRepository.count() > 0) {
            return;
        }

        // ╔═══════════╗
        // ║  Admin ║
        // ╚═══════════╝
        Admin admin1 = new Admin();
        admin1.setEmail("admin1@coode.com");
        admin1.setPassword(passwordEncoder.encode("admin123"));

        Admin admin2 = new Admin();
        admin2.setEmail("admin2@coode.com");
        admin2.setPassword(passwordEncoder.encode("admin456"));

        adminRepository.saveAll(List.of(admin1, admin2));

        // ╔═══════════╗
        // ║  User ║
        // ╚═══════════╝
        User user1 = new User();
        user1.setEmail("user1@coode.com");
        user1.setPassword(passwordEncoder.encode("user123"));
        user1.setName("小明");
        user1.setPhone("0912345678");
        user1.setCreditCard("4242424242424242");
        user1.setStatus("ACTIVE");
        user1.setGender("MALE");
        user1.setPicture("user1.jpg");
        user1.setBirthday(LocalDate.of(1995, 5, 20));

        User user2 = new User();
        user2.setEmail("user2@coode.com");
        user2.setPassword(passwordEncoder.encode("user456"));
        user2.setName("小美");
        user2.setPhone("0987654321");
        user2.setCreditCard("4111111111111111");
        user2.setStatus("ACTIVE");
        user2.setGender("FEMALE");
        user2.setPicture("user2.jpg");
        user2.setBirthday(LocalDate.of(1998, 11, 3));

        userRepository.saveAll(List.of(user1, user2));

        // ╔═══════════╗
        // ║  Vendor ║
        // ╚═══════════╝
        Vendor vendor1 = new Vendor();
        vendor1.setVendorName("潮流服飾店");
        vendor1.setEmail("vendor1@coode.com");
        vendor1.setPassword(passwordEncoder.encode("vendor123"));
        vendor1.setStatus("ACTIVE");
        vendor1.setActivatedAt(LocalDateTime.now());
        vendor1.setContractExpiresAt(LocalDateTime.now().plusYears(1));

        Vendor vendor2 = new Vendor();
        vendor2.setVendorName("舒適鞋鋪");
        vendor2.setEmail("vendor2@coode.com");
        vendor2.setPassword(passwordEncoder.encode("vendor456"));
        vendor2.setStatus("ACTIVE");
        vendor2.setActivatedAt(LocalDateTime.now());
        vendor2.setContractExpiresAt(LocalDateTime.now().plusYears(1));

        vendorRepository.saveAll(List.of(vendor1, vendor2));

        // ╔═══════════╗
        // ║  Product ║
        // ╚═══════════╝
        Product product1 = new Product();
        product1.setName("白色純棉T恤");
        product1.setPattern("純色");
        product1.setCategoryType("TOP");
        product1.setGender("MEN");
        product1.setStyle("休閒");
        product1.setColor("白");
        product1.setSize("M");
        product1.setStock(50);
        product1.setPrice(new BigDecimal("299.00"));
        product1.setDescription("100%純棉，透氣舒適");
        product1.setImagesJpg("t_shirt_white.jpg");
        product1.setOutfitPng("t_shirt_white.png");
        product1.setStatus("ACTIVE");
        product1.setVendor(vendor1);

        Product product2 = new Product();
        product2.setName("黑色牛仔外套");
        product2.setPattern("純色");
        product2.setCategoryType("OUTER");
        product2.setGender("MEN");
        product2.setStyle("街頭");
        product2.setColor("黑");
        product2.setSize("L");
        product2.setStock(30);
        product2.setPrice(new BigDecimal("899.00"));
        product2.setDescription("經典牛仔，百搭款式");
        product2.setImagesJpg("denim_jacket_black.jpg");
        product2.setOutfitPng("denim_jacket_black.png");
        product2.setStatus("ACTIVE");
        product2.setVendor(vendor1);

        Product product3 = new Product();
        product3.setName("卡其色工裝長褲");
        product3.setPattern("純色");
        product3.setCategoryType("BOTTOM");
        product3.setGender("WOMEN");
        product3.setStyle("機能");
        product3.setColor("卡其");
        product3.setSize("32");
        product3.setStock(40);
        product3.setPrice(new BigDecimal("699.00"));
        product3.setDescription("多口袋設計，耐磨耐穿");
        product3.setImagesJpg("cargo_pants_khaki.jpg");
        product3.setOutfitPng("cargo_pants_khaki.png");
        product3.setStatus("ACTIVE");
        product3.setVendor(vendor2);

        Product product4 = new Product();
        product4.setName("白色休閒運動鞋");
        product4.setPattern("純色");
        product4.setCategoryType("SHOES");
        product4.setGender("WOMEN");
        product4.setStyle("運動");
        product4.setColor("白");
        product4.setSize("US9");
        product4.setStock(60);
        product4.setPrice(new BigDecimal("1290.00"));
        product4.setDescription("輕量緩震，日常穿搭必備");
        product4.setImagesJpg("sneaker_white.jpg");
        product4.setOutfitPng("sneaker_white.png");
        product4.setStatus("ACTIVE");
        product4.setVendor(vendor2);

        Product product5 = new Product();
        product5.setName("灰色針織毛衣");
        product5.setPattern("針織");
        product5.setCategoryType("TOP");
        product5.setGender("WOMEN");
        product5.setStyle("韓系");
        product5.setColor("灰");
        product5.setSize("S");
        product5.setStock(2);
        product5.setPrice(new BigDecimal("499.00"));
        product5.setDescription("柔軟針織，秋冬必備");
        product5.setImagesJpg("knitwear_gray.jpg");
        product5.setOutfitPng("knitwear_gray.png");
        product5.setStatus("DRAFT");
        product5.setVendor(vendor1);

        Product product6 = new Product();
        product6.setName("藍色格紋襯衫");
        product6.setPattern("格紋");
        product6.setCategoryType("TOP");
        product6.setGender("MEN");
        product6.setStyle("正式");
        product6.setColor("藍");
        product6.setSize("L");
        product6.setStock(0);
        product6.setPrice(new BigDecimal("599.00"));
        product6.setDescription("商務休閒皆宜");
        product6.setImagesJpg("shirt_blue.jpg");
        product6.setOutfitPng("shirt_blue.png");
        product6.setStatus("INACTIVE");
        product6.setVendor(vendor1);

        productRepository.saveAll(List.of(product1, product2, product3, product4, product5, product6));

        // ╔═══════════╗
        // ║  Cart ║
        // ╚═══════════╝
        Cart cart1 = new Cart();
        cart1.setUser(user1);
        cart1.setTotalQuantity(2);

        Cart cart2 = new Cart();
        cart2.setUser(user2);
        cart2.setTotalQuantity(2);

        cartRepository.saveAll(List.of(cart1, cart2));

        // ╔═══════════╗
        // ║  CartItem ║
        // ╚═══════════╝
        CartItem cartItem1 = new CartItem();
        cartItem1.setCart(cart1);
        cartItem1.setProduct(product1);
        cartItem1.setProductQuantity(2);
        cartItem1.setPrice(product1.getPrice());
        cartItem1.setTotalPrice(product1.getPrice().multiply(BigDecimal.valueOf(2)));

        CartItem cartItem2 = new CartItem();
        cartItem2.setCart(cart1);
        cartItem2.setProduct(product2);
        cartItem2.setProductQuantity(1);
        cartItem2.setPrice(product2.getPrice());
        cartItem2.setTotalPrice(product2.getPrice());

        CartItem cartItem3 = new CartItem();
        cartItem3.setCart(cart2);
        cartItem3.setProduct(product3);
        cartItem3.setProductQuantity(1);
        cartItem3.setPrice(product3.getPrice());
        cartItem3.setTotalPrice(product3.getPrice());

        CartItem cartItem4 = new CartItem();
        cartItem4.setCart(cart2);
        cartItem4.setProduct(product4);
        cartItem4.setProductQuantity(2);
        cartItem4.setPrice(product4.getPrice());
        cartItem4.setTotalPrice(product4.getPrice().multiply(BigDecimal.valueOf(2)));

        cartItemRepository.saveAll(List.of(cartItem1, cartItem2, cartItem3, cartItem4));

        // ╔═══════════╗
        // ║  Order ║
        // ╚═══════════╝
        Order order1 = new Order();
        order1.setUser(user1);
        order1.setRecipientName("小明");
        order1.setRecipientPhone("0912345678");
        order1.setRecipientAddress("台北市中山區一段1號");
        order1.setTotalAmount(3);
        order1.setSumTotal(new BigDecimal("1497.00"));

        Order order2 = new Order();
        order2.setUser(user2);
        order2.setRecipientName("小美");
        order2.setRecipientPhone("0987654321");
        order2.setRecipientAddress("台中市西屯區二段2號");
        order2.setTotalAmount(3);
        order2.setSumTotal(new BigDecimal("3279.00"));

        orderRepository.saveAll(List.of(order1, order2));

        // ╔═══════════╗
        // ║  OrderItem ║
        // ╚═══════════╝
        OrderItem orderItem1 = new OrderItem();
        orderItem1.setOrder(order1);
        orderItem1.setVendor(vendor1);
        orderItem1.setProduct(product1);
        orderItem1.setProductQuantity(2);
        orderItem1.setPrice(product1.getPrice());
        orderItem1.setPriceTotal(product1.getPrice().multiply(BigDecimal.valueOf(2)));
        orderItem1.setStatus("RECEIVED");

        OrderItem orderItem2 = new OrderItem();
        orderItem2.setOrder(order1);
        orderItem2.setVendor(vendor1);
        orderItem2.setProduct(product2);
        orderItem2.setProductQuantity(1);
        orderItem2.setPrice(product2.getPrice());
        orderItem2.setPriceTotal(product2.getPrice());
        orderItem2.setStatus("RECEIVED");

        OrderItem orderItem3 = new OrderItem();
        orderItem3.setOrder(order2);
        orderItem3.setVendor(vendor2);
        orderItem3.setProduct(product3);
        orderItem3.setProductQuantity(1);
        orderItem3.setPrice(product3.getPrice());
        orderItem3.setPriceTotal(product3.getPrice());
        orderItem3.setStatus("SHIPPED");

        OrderItem orderItem4 = new OrderItem();
        orderItem4.setOrder(order2);
        orderItem4.setVendor(vendor2);
        orderItem4.setProduct(product4);
        orderItem4.setProductQuantity(2);
        orderItem4.setPrice(product4.getPrice());
        orderItem4.setPriceTotal(product4.getPrice().multiply(BigDecimal.valueOf(2)));
        orderItem4.setStatus("SHIPPED");

        orderItemRepository.saveAll(List.of(orderItem1, orderItem2, orderItem3, orderItem4));

        // ╔═══════════╗
        // ║  Outfit ║
        // ╚═══════════╝
        Outfit outfit1 = new Outfit();
        outfit1.setUser(user1);
        outfit1.setName("週末休閒穿搭");

        Outfit outfit2 = new Outfit();
        outfit2.setUser(user2);
        outfit2.setName("上班通勤穿搭");

        outfitRepository.saveAll(List.of(outfit1, outfit2));

        // ╔═══════════╗
        // ║  OutfitItem ║
        // ╚═══════════╝
        OutfitItem outfitItem1 = new OutfitItem();
        outfitItem1.setOutfit(outfit1);
        outfitItem1.setProduct(product1);
        outfitItem1.setSlotType("UPPER_BODY");

        OutfitItem outfitItem2 = new OutfitItem();
        outfitItem2.setOutfit(outfit2);
        outfitItem2.setProduct(product4);
        outfitItem2.setSlotType("SHOES");

        outfitItemRepository.saveAll(List.of(outfitItem1, outfitItem2));

        // ╔═══════════╗
        // ║  ReturnRequest ║
        // ╚═══════════╝
        ReturnRequest returnRequest1 = new ReturnRequest();
        returnRequest1.setOrder(order1);
        returnRequest1.setUser(user1);
        returnRequest1.setVendor(vendor1);
        returnRequest1.setStatus("REVIEWED");
        returnRequest1.setRequestType("RETURN");
        returnRequest1.setReturnRequestQuantity(1);

        ReturnRequest returnRequest2 = new ReturnRequest();
        returnRequest2.setOrder(order2);
        returnRequest2.setUser(user2);
        returnRequest2.setVendor(vendor2);
        returnRequest2.setStatus("PENDING");
        returnRequest2.setRequestType("EXCHANGE");
        returnRequest2.setReturnRequestQuantity(1);

        returnRequestRepository.saveAll(List.of(returnRequest1, returnRequest2));

        // ╔═══════════╗
        // ║  ReturnItem ║
        // ╚═══════════╝
        ReturnItem returnItem1 = new ReturnItem();
        returnItem1.setReturnRequest(returnRequest1);
        returnItem1.setOrderItem(orderItem1);
        returnItem1.setStatus("APPROVED");
        returnItem1.setReason("尺寸不合");
        returnItem1.setDescription("換成更大尺寸");
        returnItem1.setApprovalQuantity(1);
        returnItem1.setRejectedQuantity(0);
        returnItem1.setRefund(new BigDecimal("299.00"));

        ReturnItem returnItem2 = new ReturnItem();
        returnItem2.setReturnRequest(returnRequest2);
        returnItem2.setOrderItem(orderItem3);
        returnItem2.setStatus("PENDING_REVIEW");
        returnItem2.setReason("商品瑕疵");
        returnItem2.setDescription("縫線脫落");
        returnItem2.setApprovalQuantity(0);
        returnItem2.setRejectedQuantity(0);
        returnItem2.setRefund(BigDecimal.ZERO);

        returnItemRepository.saveAll(List.of(returnItem1, returnItem2));

        System.out.println("======================================");
        System.out.println("Test fake data inserted successfully!");
        System.out.println("Users: 2, Vendors: 2, Admins: 2, Products: 6,");
        System.out.println("Carts: 2, CartItems: 4, Orders: 2, OrderItems: 4,");
        System.out.println("Outfits: 2, OutfitItems: 2, ReturnRequests: 2, ReturnItems: 2");
        System.out.println("======================================");
    }
}

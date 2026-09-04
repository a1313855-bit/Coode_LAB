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
import com.example.demo.model.ProductVariant;
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
import com.example.demo.repository.ProductVariantRepository;
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
    private final ProductVariantRepository productVariantRepository;
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
                        ProductVariantRepository productVariantRepository,
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
        this.productVariantRepository = productVariantRepository;
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
        // ║  Product（商品主表只存共用資訊）║
        // ╚═══════════╝
        Product product1 = new Product();
        product1.setName("純棉T恤");
        product1.setPattern("MEN");
        product1.setCategoryType("TOP");
        product1.setStyle("休閒");
        product1.setPrice(new BigDecimal("299.00"));
        product1.setDescription("100%純棉，透氣舒適");
        product1.setImagesJpg("/images/products/T-shirt/T-shirt.png");
        product1.setOutfitPng("t_shirt_white.png");
        product1.setStatus("ACTIVE");
        product1.setVendor(vendor1);

        Product product2 = new Product();
        product2.setName("牛仔外套");
        product2.setPattern("MEN");
        product2.setCategoryType("OUTER");
        product2.setStyle("街頭");
        product2.setPrice(new BigDecimal("899.00"));
        product2.setDescription("經典牛仔，百搭款式");
        product2.setImagesJpg("/images/products/Denim jacket.png");
        product2.setOutfitPng("denim_jacket_black.png");
        product2.setStatus("ACTIVE");
        product2.setVendor(vendor1);

        Product product3 = new Product();
        product3.setName("工裝長褲");
        product3.setPattern("WOMEN");
        product3.setCategoryType("BOTTOM");
        product3.setStyle("機能");
        product3.setPrice(new BigDecimal("699.00"));
        product3.setDescription("多口袋設計，耐磨耐穿");
        product3.setImagesJpg("/images/products/pants_beige.png");
        product3.setOutfitPng("cargo_pants_khaki.png");
        product3.setStatus("ACTIVE");
        product3.setVendor(vendor2);

        Product product4 = new Product();
        product4.setName("連身洋裝");
        product4.setPattern("WOMEN");
        product4.setCategoryType("DRESS");
        product4.setStyle("韓系");
        product4.setPrice(new BigDecimal("999.00"));
        product4.setDescription("剪裁俐落，一件即可完成穿搭");
        product4.setImagesJpg("/images/products/One-piece dress.png");
        product4.setOutfitPng("dress_black.png");
        product4.setStatus("ACTIVE");
        product4.setVendor(vendor2);

        Product product5 = new Product();
        product5.setName("針織毛衣");
        product5.setPattern("WOMEN");
        product5.setCategoryType("TOP");
        product5.setStyle("韓系");
        product5.setPrice(new BigDecimal("499.00"));
        product5.setDescription("柔軟針織，秋冬必備");
        product5.setImagesJpg("/images/products/Knitted sweater.png");
        product5.setOutfitPng("knitwear_gray.png");
        product5.setStatus("DRAFT");
        product5.setVendor(vendor1);

        Product product6 = new Product();
        product6.setName("格紋襯衫");
        product6.setPattern("MEN");
        product6.setCategoryType("TOP");
        product6.setStyle("正式");
        product6.setPrice(new BigDecimal("599.00"));
        product6.setDescription("商務休閒皆宜");
        product6.setImagesJpg("/images/products/Plaid shirt.png");
        product6.setOutfitPng("shirt_blue.png");
        product6.setStatus("INACTIVE");
        product6.setVendor(vendor1);

        Product product7 = new Product();
        product7.setName("棒球帽");
        product7.setPattern("KIDS");
        product7.setCategoryType("HEADWEAR");
        product7.setStyle("街頭");
        product7.setPrice(new BigDecimal("399.00"));
        product7.setDescription("百搭帽款，男女童皆適");
        product7.setImagesJpg("/images/products/Baseball cap.png");
        product7.setOutfitPng("cap_black.png");
        product7.setStatus("ACTIVE");
        product7.setVendor(vendor1);

        productRepository.saveAll(List.of(product1, product2, product3, product4, product5, product6, product7));

        // ╔═══════════╗
        // ║  ProductVariant（規格表：顏色 × 尺寸 × 庫存 × 圖片 × 販售狀態）║
        // ╚═══════════╝
        ProductVariant v1a = variant(product1, "白", "M", 48, "/images/products/T-shirt/T-shirt-white.png", "t_shirt_white_m.png", "ACTIVE");
        ProductVariant v1b = variant(product1, "白", "L", 28, "/images/products/T-shirt/T-shirt-white.png", "t_shirt_white_l.png", "ACTIVE");
        ProductVariant v1c = variant(product1, "黑", "M", 20, "/images/products/T-shirt/T-shirt-black.png", "t_shirt_black_m.png", "ACTIVE");
        ProductVariant v1d = variant(product1, "黑", "L", 12, "/images/products/T-shirt/T-shirt-black.png", "t_shirt_black_l.png", "ACTIVE");
        // 示範「黑色停售、白色照賣」：黑/L 設為停售
        ProductVariant v1e = variant(product1, "黑", "XL", 0, "/images/products/T-shirt/T-shirt-black.png", "t_shirt_black_xl.png", "INACTIVE");

        ProductVariant v2a = variant(product2, "黑", "M", 16, "/images/products/Denim jacket.png", "denim_jacket_black_m.png", "ACTIVE");
        ProductVariant v2b = variant(product2, "黑", "L", 8, "/images/products/Denim jacket.png", "denim_jacket_black_l.png", "ACTIVE");
        ProductVariant v2c = variant(product2, "藍", "M", 6, "/images/products/Denim jacket.png", "denim_jacket_blue_m.png", "ACTIVE");

        ProductVariant v3a = variant(product3, "卡其", "30", 18, "/images/products/pants_beige.png", "cargo_pants_khaki_30.png", "ACTIVE");
        ProductVariant v3b = variant(product3, "卡其", "32", 22, "/images/products/pants_beige.png", "cargo_pants_khaki_32.png", "ACTIVE");
        ProductVariant v3c = variant(product3, "軍綠", "32", 5, "/images/products/pants_beige.png", "cargo_pants_green_32.png", "ACTIVE");

        ProductVariant v4a = variant(product4, "黑", "S", 15, "/images/products/One-piece dress.png", "dress_black_s.png", "ACTIVE");
        ProductVariant v4b = variant(product4, "黑", "M", 12, "/images/products/One-piece dress.png", "dress_black_m.png", "ACTIVE");
        ProductVariant v4c = variant(product4, "白", "M", 9, "/images/products/One-piece dress.png", "dress_white_m.png", "ACTIVE");

        ProductVariant v5a = variant(product5, "灰", "S", 2, "/images/products/Knitted sweater.png", "knitwear_gray_s.png", "ACTIVE");
        ProductVariant v5b = variant(product5, "灰", "M", 4, "/images/products/Knitted sweater.png", "knitwear_gray_m.png", "ACTIVE");
        ProductVariant v5c = variant(product5, "米白", "M", 10, "/images/products/Knitted sweater.png", "knitwear_cream_m.png", "ACTIVE");

        ProductVariant v6a = variant(product6, "藍", "M", 0, "/images/products/Plaid shirt.png", "shirt_blue_m.png", "ACTIVE");
        ProductVariant v6b = variant(product6, "藍", "L", 20, "/images/products/Plaid shirt.png", "shirt_blue_l.png", "ACTIVE");
        ProductVariant v6c = variant(product6, "白", "L", 8, "/images/products/Plaid shirt.png", "shirt_white_l.png", "ACTIVE");

        ProductVariant v7a = variant(product7, "黑", "F", 25, "/images/products/Baseball cap.png", "cap_black_f.png", "ACTIVE");
        ProductVariant v7b = variant(product7, "黑", "U", 18, "/images/products/Baseball cap.png", "cap_black_u.png", "ACTIVE");
        ProductVariant v7c = variant(product7, "白", "U", 30, "/images/products/Baseball cap.png", "cap_white_u.png", "ACTIVE");

        productVariantRepository.saveAll(List.of(
                v1a, v1b, v1c, v1d, v1e,
                v2a, v2b, v2c,
                v3a, v3b, v3c,
                v4a, v4b, v4c,
                v5a, v5b, v5c,
                v6a, v6b, v6c,
                v7a, v7b, v7c));

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
        // ║  CartItem（以規格為單位）║
        // ╚═══════════╝
        CartItem cartItem1 = new CartItem();
        cartItem1.setCart(cart1);
        cartItem1.setVariant(v1a);
        cartItem1.setProductQuantity(2);
        cartItem1.setPrice(product1.getPrice());
        cartItem1.setTotalPrice(product1.getPrice().multiply(BigDecimal.valueOf(2)));

        CartItem cartItem2 = new CartItem();
        cartItem2.setCart(cart1);
        cartItem2.setVariant(v2a);
        cartItem2.setProductQuantity(1);
        cartItem2.setPrice(product2.getPrice());
        cartItem2.setTotalPrice(product2.getPrice());

        CartItem cartItem3 = new CartItem();
        cartItem3.setCart(cart2);
        cartItem3.setVariant(v3a);
        cartItem3.setProductQuantity(1);
        cartItem3.setPrice(product3.getPrice());
        cartItem3.setTotalPrice(product3.getPrice());

        CartItem cartItem4 = new CartItem();
        cartItem4.setCart(cart2);
        cartItem4.setVariant(v4a);
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
        // ║  OrderItem（以規格為單位）║
        // ╚═══════════╝
        OrderItem orderItem1 = new OrderItem();
        orderItem1.setOrder(order1);
        orderItem1.setVendor(vendor1);
        orderItem1.setVariant(v1a);
        orderItem1.setProductQuantity(2);
        orderItem1.setPrice(product1.getPrice());
        orderItem1.setPriceTotal(product1.getPrice().multiply(BigDecimal.valueOf(2)));
        orderItem1.setStatus("RECEIVED");

        OrderItem orderItem2 = new OrderItem();
        orderItem2.setOrder(order1);
        orderItem2.setVendor(vendor1);
        orderItem2.setVariant(v2a);
        orderItem2.setProductQuantity(1);
        orderItem2.setPrice(product2.getPrice());
        orderItem2.setPriceTotal(product2.getPrice());
        orderItem2.setStatus("RECEIVED");

        OrderItem orderItem3 = new OrderItem();
        orderItem3.setOrder(order2);
        orderItem3.setVendor(vendor2);
        orderItem3.setVariant(v3a);
        orderItem3.setProductQuantity(1);
        orderItem3.setPrice(product3.getPrice());
        orderItem3.setPriceTotal(product3.getPrice());
        orderItem3.setStatus("SHIPPED");

        OrderItem orderItem4 = new OrderItem();
        orderItem4.setOrder(order2);
        orderItem4.setVendor(vendor2);
        orderItem4.setVariant(v4a);
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
        // ║  OutfitItem（含規格：記錄顏色）║
        // ╚═══════════╝
        OutfitItem outfitItem1 = new OutfitItem();
        outfitItem1.setOutfit(outfit1);
        outfitItem1.setProduct(product1);
        outfitItem1.setVariant(v1a);
        outfitItem1.setSlotType("UPPER_BODY");

        OutfitItem outfitItem2 = new OutfitItem();
        outfitItem2.setOutfit(outfit2);
        outfitItem2.setProduct(product4);
        outfitItem2.setVariant(v4b);
        outfitItem2.setSlotType("FULL_BODY");

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
        System.out.println("Users: 2, Vendors: 2, Admins: 2, Products: 6, Variants: 19,");
        System.out.println("Carts: 2, CartItems: 4, Orders: 2, OrderItems: 4,");
        System.out.println("Outfits: 2, OutfitItems: 2, ReturnRequests: 2, ReturnItems: 2");
        System.out.println("======================================");
    }

    private ProductVariant variant(Product product, String color, String size,
            int stock, String imagesJpg, String outfitPng, String status) {
        ProductVariant v = new ProductVariant();
        v.setProduct(product);
        v.setColor(color);
        v.setSize(size);
        v.setStock(stock);
        v.setImagesJpg(imagesJpg);
        v.setOutfitPng(outfitPng);
        v.setStatus(status);
        return v;
    }
}
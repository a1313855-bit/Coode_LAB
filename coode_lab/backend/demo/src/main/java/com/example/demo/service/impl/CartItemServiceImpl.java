package com.example.demo.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.AddCartItemRequest;
import com.example.demo.dto.CartItemResponse;
import com.example.demo.dto.UpdateCartItemRequest;
import com.example.demo.model.Cart;
import com.example.demo.model.CartItem;
import com.example.demo.model.Product;
import com.example.demo.model.ProductVariant;
import com.example.demo.repository.CartItemRepository;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.ProductVariantRepository;
import com.example.demo.service.CartItemService;
import com.example.demo.util.SelectPartOfData;

@Service
public class CartItemServiceImpl implements CartItemService {

        // ╔═══════════════════╗
        // ║ 依賴注入          ║
        // ╚═══════════════════╝

        private final CartItemRepository cartItemRepository;
        private final CartRepository cartRepository;
        private final ProductVariantRepository productVariantRepository;

        public CartItemServiceImpl(
                        CartItemRepository cartItemRepository,
                        CartRepository cartRepository,
                        ProductVariantRepository productVariantRepository) {

                this.cartItemRepository = cartItemRepository;
                this.cartRepository = cartRepository;
                this.productVariantRepository = productVariantRepository;
        }

        // ╔═══════════════════════╗
        // ║ 購物車商品 查詢 READ ║
        // ╚═══════════════════════╝

        // 查詢購物車全部商品 (固定每頁10筆)
        @Override
        public SelectPartOfData.Result<CartItemResponse> findCartItemsByCartId(
                        Long cartId, int page) {

                List<CartItemResponse> all = cartItemRepository
                                .findByCart_CartId(cartId)
                                .stream()
                                .map(this::toCartItemResponse)
                                .toList();
                return SelectPartOfData.pageOf10(all, page);
        }

        // 查詢購物車中的指定規格
        @Override
        public CartItemResponse findCartItemByCartIdAndVariantId(
                        Long cartId,
                        Long variantId) {

                CartItem cartItem = cartItemRepository
                                .findByCart_CartIdAndVariant_VariantId(
                                                cartId,
                                                variantId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "購物車內找不到此規格商品"));

                return toCartItemResponse(cartItem);
        }

        // 計算購物車裡有幾種商品
        @Override
        public Long countDistinctProducts(Long cartId) {

                return cartItemRepository
                                .countByCart_CartId(cartId);
        }

        // 依商品名稱關鍵字搜尋 (固定每頁10筆)
        @Override
        public SelectPartOfData.Result<CartItemResponse> findCartItemsByKeyword(
                        Long cartId,
                        String keyword,
                        int page) {

                List<CartItemResponse> all = cartItemRepository
                                .findByCart_CartIdAndVariant_Product_NameContaining(
                                                cartId,
                                                keyword)
                                .stream()
                                .map(this::toCartItemResponse)
                                .toList();
                return SelectPartOfData.pageOf10(all, page);
        }

        // ╔═════════════════════════╗
        // ║ 購物車商品 CREATE ║
        // ╚═════════════════════════╝

        @Override
        @Transactional
        public CartItemResponse addCartItem(
                        AddCartItemRequest request) {

                // 數量必須大於 0
                if (request.getProductQuantity() == null
                                || request.getProductQuantity() <= 0) {

                        throw new IllegalArgumentException(
                                        "商品數量必須大於 0");
                }

                // 找 Cart
                Cart cart = cartRepository
                                .findById(request.getCartId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "找不到購物車"));

                // 找規格（決定顏色/尺寸/庫存/價格來源）
                ProductVariant variant = productVariantRepository
                                .findById(request.getVariantId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "找不到商品規格"));

                // 商品與規格都必須可販售（雙層 status）
                Product product = variant.getProduct();
                if (!"ACTIVE".equals(product.getStatus())) {
                        throw new IllegalArgumentException("商品目前已停售");
                }
                if (!"ACTIVE".equals(variant.getStatus())) {
                        throw new IllegalArgumentException("此規格目前已停售");
                }

                // 查詢同一規格是否已經存在
                CartItem cartItem = cartItemRepository
                                .findByCart_CartIdAndVariant_VariantId(
                                                request.getCartId(),
                                                request.getVariantId())
                                .orElse(null);

                if (cartItem != null) {

                        // =========================
                        // 已存在：只增加商品件數
                        // =========================

                        int newQuantity = cartItem.getProductQuantity()
                                        + request.getProductQuantity();
                        // 若庫存為零，則不能加入購物車
                        if (variant.getStock() == null
                                        || variant.getStock() <= 0) {
                                throw new IllegalArgumentException("此規格目前無庫存");
                        }
                        // 即時檢查規格庫存
                        if (newQuantity > variant.getStock()) {
                                throw new IllegalArgumentException(
                                                "加入數量超過目前庫存，現有庫存： "
                                                                + variant.getStock());
                        }
                        // 抓即時價格
                        BigDecimal currentPrice = product.getPrice();

                        cartItem.setProductQuantity(newQuantity);

                        // 同步最新單價
                        cartItem.setPrice(currentPrice);

                        // 使用最新價格計算
                        cartItem.setTotalPrice(
                                        cartItem.getPrice()
                                                        .multiply(
                                                                        BigDecimal.valueOf(
                                                                                        newQuantity)));

                } else {
                        // 第一次加入購物車
                        if (variant.getStock() == null || variant.getStock() <= 0) {
                                throw new IllegalArgumentException("此規格目前無庫存");
                        }
                        if (request.getProductQuantity() > variant.getStock()) {
                                throw new IllegalArgumentException("加入數量超過目前庫存，現有庫存： "
                                                + variant.getStock());
                        }
                        // =========================
                        // 不存在：新增一種規格商品
                        // =========================

                        BigDecimal currentPrice = product.getPrice();

                        cartItem = new CartItem();

                        cartItem.setCart(cart);
                        cartItem.setVariant(variant);

                        cartItem.setProductQuantity(
                                        request.getProductQuantity());

                        // 價格由後端 Product 決定
                        cartItem.setPrice(currentPrice);

                        cartItem.setTotalPrice(
                                        currentPrice
                                                        .multiply(
                                                                        BigDecimal.valueOf(
                                                                                        request.getProductQuantity())));
                }

                CartItem savedItem = cartItemRepository.save(cartItem);

                updateCartTotalQuantity(cart);

                return toCartItemResponse(savedItem);
        }

        // ╔═════════════════════════╗
        // ║ 購物車商品 UPDATE ║
        // ╚═════════════════════════╝

        @Override
        @Transactional
        public CartItemResponse updateCartItem(
                        Long cartItemId,
                        UpdateCartItemRequest request) {

                if (request.getProductQuantity() == null
                                || request.getProductQuantity() <= 0) {

                        throw new IllegalArgumentException(
                                        "商品數量必須大於 0");
                }

                CartItem existingItem = cartItemRepository
                                .findById(cartItemId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "找不到購物車商品"));
                ProductVariant variant = existingItem.getVariant();
                Product product = variant.getProduct();

                // 商品與規格停售檢查
                if (!"ACTIVE".equals(product.getStatus())) {
                        throw new IllegalArgumentException("商品目前已停售");
                }
                if (!"ACTIVE".equals(variant.getStatus())) {
                        throw new IllegalArgumentException("此規格目前已停售");
                }

                // 即時庫存檢查
                if (variant.getStock() == null || variant.getStock() <= 0) {
                        throw new IllegalArgumentException(
                                        "此規格目前無庫存");
                }

                if (request.getProductQuantity() > variant.getStock()) {
                        throw new IllegalArgumentException(
                                        "商品數量超過目前庫存，現有庫存： " + variant.getStock());
                }

                // 取得 Product 最新價格
                BigDecimal currentPrice = product.getPrice();

                existingItem.setProductQuantity(
                                request.getProductQuantity());

                // 更新CartItem保存的價格
                existingItem.setPrice(currentPrice);

                // 使用目前價格重新計算
                existingItem.setTotalPrice(
                                existingItem.getPrice()
                                                .multiply(
                                                                BigDecimal.valueOf(
                                                                                request.getProductQuantity())));

                CartItem savedItem = cartItemRepository.save(existingItem);

                updateCartTotalQuantity(
                                existingItem.getCart());

                return toCartItemResponse(savedItem);
        }

        // ╔═════════════════════════╗
        // ║ 購物車商品 DELETE ║
        // ╚═════════════════════════╝

        // 刪除指定規格
        @Override
        @Transactional
        public void deleteCartItem(
                        Long cartId,
                        Long variantId) {

                Cart cart = cartRepository
                                .findById(cartId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "找不到購物車"));

                CartItem existingItem = cartItemRepository
                                .findByCart_CartIdAndVariant_VariantId(
                                                cartId,
                                                variantId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "購物車內找不到此規格商品"));

                cartItemRepository.delete(existingItem);

                updateCartTotalQuantity(cart);
        }

        // 清空購物車
        @Override
        @Transactional
        public void clearCart(Long cartId) {

                Cart cart = cartRepository
                                .findById(cartId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "找不到購物車"));

                cartItemRepository
                                .deleteByCart_CartId(cartId);

                // 清空後沒有任何商品種類
                cart.setTotalQuantity(0);

                cartRepository.save(cart);
        }

        // ╔═════════════════════════════╗
        // ║ 同步 Cart.totalQuantity ║
        // ╚═════════════════════════════╝

        private void updateCartTotalQuantity(Cart cart) {

                Long distinctProductCount = cartItemRepository
                                .countByCart_CartId(
                                                cart.getCartId());

                cart.setTotalQuantity(
                                distinctProductCount.intValue());

                cartRepository.save(cart);
        }

        // ╔═════════════════════════════╗
        // ║ Entity → Response DTO ║
        // ╚═════════════════════════════╝

        private CartItemResponse toCartItemResponse(CartItem cartItem) {

                CartItemResponse response = new CartItemResponse();

                ProductVariant variant = cartItem.getVariant();
                Product product = variant.getProduct();

                // 抓商品(Product)目前即時價格
                BigDecimal currentPrice = product.getPrice();

                // 使用目前價格重新計算總價
                BigDecimal currentTotalPrice = currentPrice.multiply(BigDecimal.valueOf(cartItem.getProductQuantity()));

                response.setCartItemId(cartItem.getCartItemId());

                response.setCartId(cartItem.getCart().getCartId());

                response.setVariantId(variant.getVariantId());

                response.setColor(variant.getColor());

                response.setSize(variant.getSize());

                response.setProductId(product.getProductId());

                response.setProductName(product.getName());

                response.setProductStatus(product.getStatus());

                response.setVariantStatus(variant.getStatus());

                response.setProductQuantity(cartItem.getProductQuantity());

                // 即時庫存，供前端連動庫存檢查
                response.setStock(variant.getStock());

                // 不再直接使用 CartItem 舊價格
                response.setPrice(currentPrice);

                // 不再直接使用 CartItem 舊 totalPrice
                response.setTotalPrice(currentTotalPrice);

                return response;
        }
}
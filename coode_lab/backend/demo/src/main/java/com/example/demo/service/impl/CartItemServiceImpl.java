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
import com.example.demo.repository.CartItemRepository;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.service.CartItemService;
import com.example.demo.util.SelectPartOfData;

@Service
public class CartItemServiceImpl implements CartItemService {

        // ╔═══════════════╗
        // ║ 依賴注入 ║
        // ╚═══════════════╝

        private final CartItemRepository cartItemRepository;
        private final CartRepository cartRepository;
        private final ProductRepository productRepository;

        public CartItemServiceImpl(
                        CartItemRepository cartItemRepository,
                        CartRepository cartRepository,
                        ProductRepository productRepository) {

                this.cartItemRepository = cartItemRepository;
                this.cartRepository = cartRepository;
                this.productRepository = productRepository;
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

        // 查詢購物車中的指定商品
        @Override
        public CartItemResponse findCartItemByCartIdAndProductId(
                        Long cartId,
                        Long productId) {

                CartItem cartItem = cartItemRepository
                                .findByCart_CartIdAndProduct_ProductId(
                                                cartId,
                                                productId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "購物車內找不到此商品"));

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
                                .findByCart_CartIdAndProduct_NameContaining(
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

                // 找 Product
                Product product = productRepository
                                .findById(request.getProductId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "找不到商品"));

                // 查詢同一商品是否已經存在
                CartItem cartItem = cartItemRepository
                                .findByCart_CartIdAndProduct_ProductId(
                                                request.getCartId(),
                                                request.getProductId())
                                .orElse(null);

                if (cartItem != null) {

                        // =========================
                        // 已存在：只增加商品件數
                        // =========================

                        int newQuantity = cartItem.getProductQuantity()
                                        + request.getProductQuantity();
                        // 若庫存為零，則不能加入購物車
                        if (product.getStock() == null
                                        || product.getStock() <= 0) {
                                throw new IllegalArgumentException("商品目前無庫存");
                        }
                        // 即時檢查商品庫存
                        if (newQuantity > product.getStock()) {
                                throw new IllegalArgumentException(
                                                "加入數量超過目前商品庫存，現有庫存： "
                                                                + product.getStock());
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
                        if (request.getProductQuantity() > product.getStock()) {
                                throw new IllegalArgumentException("加入數量超過目前庫存，現有庫存： "
                                                + product.getStock());
                        }
                        // =========================
                        // 不存在：新增一種商品
                        // =========================

                        BigDecimal currentPrice = product.getPrice();

                        cartItem = new CartItem();

                        cartItem.setCart(cart);
                        cartItem.setProduct(product);

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

                /*
                 * 重新計算商品種類數。
                 *
                 * 如果原本已有相同商品：
                 * CartItem 筆數不變，所以 totalQuantity 不變。
                 *
                 * 如果是新商品：
                 * CartItem 多一筆，所以 totalQuantity + 1。
                 */
                updateCartTotalQuantity(cart);

                return toCartItemResponse(savedItem);
                // throw new UnsupportedOperationException("等ProductRepository合併後啟用加入購物車功能");
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
                Product product = existingItem.getProduct();

                // 即時庫存檢查
                if (product.getStock() == null || product.getStock() <= 0) {
                        throw new IllegalArgumentException(
                                        "商品目前無庫存");
                }

                if (request.getProductQuantity() > product.getStock()) {
                        throw new IllegalArgumentException(
                                        "商品數量超過目前庫存，現有庫存： " + product.getStock());
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

                /*
                 * 修改 2 件 → 10 件，
                 * 商品種類仍然只有這一種，
                 * totalQuantity 不會改變。
                 *
                 * 這裡仍同步一次，確保資料一致。
                 */
                updateCartTotalQuantity(
                                existingItem.getCart());

                return toCartItemResponse(savedItem);
        }

        // ╔═════════════════════════╗
        // ║ 購物車商品 DELETE ║
        // ╚═════════════════════════╝

        // 刪除指定商品
        @Override
        @Transactional
        public void deleteCartItem(
                        Long cartId,
                        Long productId) {

                Cart cart = cartRepository
                                .findById(cartId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "找不到購物車"));

                CartItem existingItem = cartItemRepository
                                .findByCart_CartIdAndProduct_ProductId(
                                                cartId,
                                                productId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "購物車內找不到此商品"));

                cartItemRepository.delete(existingItem);

                /*
                 * 刪除一整種商品後：
                 * CartItem 筆數 - 1
                 * totalQuantity 也會 - 1
                 */
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

                /*
                 * 注意：
                 *
                 * totalQuantity = 商品種類數
                 * = CartItem 筆數
                 *
                 * 絕對不是 productQuantity 的總和。
                 */

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

                Product product = cartItem.getProduct();

                // 抓商品(Product)目前即時價格
                BigDecimal currentPrice = product.getPrice();

                // 使用目前價格重新計算總價
                BigDecimal currentTotalPrice = currentPrice.multiply(BigDecimal.valueOf(cartItem.getProductQuantity()));

                response.setCartItemId(cartItem.getCartItemId());

                response.setCartId(cartItem.getCart().getCartId());

                response.setProductId(product.getProductId());

                response.setProductName(product.getName());

                response.setProductQuantity(cartItem.getProductQuantity());

                // 即時庫存，供前端連動庫存檢查
                response.setStock(product.getStock());

                // 不再直接使用 CartItem 舊價格
                response.setPrice(currentPrice);

                // 不再直接使用 CartItem 舊 totalPrice
                response.setTotalPrice(currentTotalPrice);

                return response;
        }
}
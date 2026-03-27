package org.bbqqvv.backendecommerce.service.impl;

import org.bbqqvv.backendecommerce.exception.codes.*;

import lombok.extern.slf4j.Slf4j;
import org.bbqqvv.backendecommerce.config.jwt.SecurityUtils;
import org.bbqqvv.backendecommerce.dto.request.CartRequest;
import org.bbqqvv.backendecommerce.dto.response.CartResponse;
import org.bbqqvv.backendecommerce.entity.*;
import org.bbqqvv.backendecommerce.exception.AppException;
import org.bbqqvv.backendecommerce.exception.ErrorCode;
import org.bbqqvv.backendecommerce.mapper.CartMapper;
import org.bbqqvv.backendecommerce.repository.*;
import org.bbqqvv.backendecommerce.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final SizeProductRepository sizeProductRepository;
    private final SizeCategoryRepository sizeCategoryRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;

    public CartServiceImpl(CartRepository cartRepository,
                           CartItemRepository cartItemRepository,
                           ProductRepository productRepository,
                           SizeProductRepository sizeProductRepository,
                           SizeCategoryRepository sizeCategoryRepository,
                           UserRepository userRepository,
                           CartMapper cartMapper) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.sizeProductRepository = sizeProductRepository;
        this.sizeCategoryRepository = sizeCategoryRepository;
        this.userRepository = userRepository;
        this.cartMapper = cartMapper;
    }

    @Override
    @Transactional
    public CartResponse addOrUpdateProductInCart(CartRequest cartRequest) {
        User user = getAuthenticatedUser();
        Cart cart = cartRepository.findByUserId(user.getId()).orElseGet(() ->
                cartRepository.save(Cart.builder()
                        .user(user)
                        .totalPrice(BigDecimal.ZERO)
                        .cartItems(new ArrayList<>())
                        .build())
        );

        Map<String, CartItem> cartItemMap = cart.getCartItems().stream()
                .collect(Collectors.toMap(
                        item -> generateKey(item.getProduct().getId(), item.getSizeName(), item.getColor()),
                        item -> item
                ));

        cartRequest.getItems().forEach(itemRequest -> {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new AppException(ProductErrorCode.PRODUCT_NOT_FOUND));

            validateSizeOption(product, itemRequest.getSizeName());

            SizeProduct sizeProduct = sizeProductRepository.findByProductIdAndSizeName(
                            product.getId(), itemRequest.getSizeName())
                    .orElseThrow(() -> new AppException(ProductErrorCode.INVALID_PRODUCT_OPTION));

            // ⚡ Tìm SizeProductVariant cụ thể khớp với PRODUCT + COLOR + SIZE để lấy giá và stock chính xác
            // Vì SizeProduct (Vd: "S") có thể dùng chung cho nhiều sản phẩm, nên phải lọc thêm productId
            SizeProductVariant matchedSPV = sizeProduct.getProductVariantSizes().stream()
                    .filter(spv -> spv.getProductVariant().getProduct().getId().equals(product.getId()) &&
                                   spv.getProductVariant().getColor().equalsIgnoreCase(itemRequest.getColor()))
                    .findFirst()
                    .orElse(null);

            if (matchedSPV == null) {
                log.warn("⚠️ Không tìm thấy biến thể khớp chính xác. Đang thử map fallback...");
                matchedSPV = sizeProduct.getProductVariantSizes().stream()
                    .filter(spv -> spv.getProductVariant().getProduct().getId().equals(product.getId()))
                    .findFirst()
                    .orElseThrow(() -> new AppException(ProductErrorCode.PRODUCT_VARIANT_NOT_FOUND));
            }

            ProductVariant productVariant = matchedSPV != null 
                    ? matchedSPV.getProductVariant()
                    : sizeProduct.getProductVariantSizes().stream()
                        .map(SizeProductVariant::getProductVariant)
                        .findFirst()
                        .orElseThrow(() -> new AppException(ProductErrorCode.PRODUCT_VARIANT_NOT_FOUND));

            String key = generateKey(itemRequest.getProductId(), itemRequest.getSizeName(), itemRequest.getColor());
            int newTotalQuantity = cartItemMap.getOrDefault(key, new CartItem()).getQuantity() + itemRequest.getQuantity();

            int stockAvailable = matchedSPV.getStock();
            if (newTotalQuantity > stockAvailable) {
                log.error("❌ Hết hàng cho biến thể này. Yêu cầu: {}, Có sẵn: {}", newTotalQuantity, stockAvailable);
                throw new AppException(ProductErrorCode.OUT_OF_STOCK);
            }

            // ⚡ Lấy giá từ SizeProductVariant (bảng chi tiết), không phải SizeProduct (bảng global)
            final BigDecimal price;
            if (matchedSPV != null) {
                price = matchedSPV.getPriceAfterDiscount() != null && matchedSPV.getPriceAfterDiscount().compareTo(BigDecimal.ZERO) > 0
                        ? matchedSPV.getPriceAfterDiscount()
                        : (matchedSPV.getPrice() != null ? matchedSPV.getPrice() : BigDecimal.ZERO);
            } else {
                price = BigDecimal.ZERO;
            }
            boolean isInStock = sizeProduct.getStockQuantity() >= itemRequest.getQuantity();

            cartItemMap.computeIfAbsent(key, k -> {
                CartItem newCartItem = CartItem.builder()
                        .cart(cart)
                        .product(product)
                        .productVariant(productVariant)
                        .quantity(0) // Sẽ cập nhật ngay sau đây
                        .sizeName(itemRequest.getSizeName())
                        .color(itemRequest.getColor())
                        .price(price)
                        .subtotal(BigDecimal.ZERO)
                        .build();
                cart.getCartItems().add(newCartItem);
                return newCartItem;
            }).setQuantity(newTotalQuantity);


            cartItemMap.get(key).setSubtotal(price.multiply(BigDecimal.valueOf(newTotalQuantity)));
        });

        updateCartTotal(cart);
        return cartMapper.toCartResponse(cart);
    }


    @Override
    @Transactional
    public CartResponse removeProductFromCart(Long productId, String sizeName, String color) {
        User user = getAuthenticatedUser();
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(CartOrderErrorCode.CART_NOT_FOUND));

        CartItem cartItem = cartItemRepository.findByCartIdAndProductIdAndSizeNameAndColor(cart.getId(), productId, sizeName, color)
                .orElseThrow(() -> new AppException(CartOrderErrorCode.CART_ITEM_NOT_FOUND));

        cart.getCartItems().remove(cartItem);
        cartItemRepository.delete(cartItem);
        updateCartTotal(cart);

        return cartMapper.toCartResponse(cart);
    }
    @Override
    @Transactional
    public CartResponse getCartByUserId() {
        User user = getAuthenticatedUser();
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(Cart.builder()
                        .user(user)
                        .totalPrice(BigDecimal.ZERO)
                        .cartItems(new ArrayList<>())
                        .build()));

        return cartMapper.toCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse increaseProductQuantity(CartRequest cartRequest) {
        User user = getAuthenticatedUser();
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(CartOrderErrorCode.CART_NOT_FOUND));

        for (var itemRequest : cartRequest.getItems()) {
            CartItem cartItem = cartItemRepository.findByCartIdAndProductIdAndSizeNameAndColor(
                            cart.getId(), itemRequest.getProductId(), itemRequest.getSizeName(), itemRequest.getColor())
                    .orElseThrow(() -> new AppException(CartOrderErrorCode.CART_ITEM_NOT_FOUND));

            SizeProduct sizeProduct = sizeProductRepository.findByProductIdAndSizeName(
                            cartItem.getProduct().getId(), cartItem.getSizeName())
                    .orElseThrow(() -> new AppException(ProductErrorCode.INVALID_PRODUCT_OPTION));

            // Kiểm tra tồn kho
            if (cartItem.getQuantity() + 1 > sizeProduct.getStockQuantity()) {
                throw new AppException(ProductErrorCode.OUT_OF_STOCK);
            }

            cartItem.setQuantity(cartItem.getQuantity() + 1);
            cartItem.setSubtotal(cartItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            cartItemRepository.save(cartItem);
        }

        updateCartTotal(cart);
        return cartMapper.toCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse decreaseProductQuantity(CartRequest cartRequest) {
        User user = getAuthenticatedUser();
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(CartOrderErrorCode.CART_NOT_FOUND));

        for (var itemRequest : cartRequest.getItems()) {
            CartItem cartItem = cartItemRepository.findByCartIdAndProductIdAndSizeNameAndColor(
                            cart.getId(), itemRequest.getProductId(), itemRequest.getSizeName(), itemRequest.getColor())
                    .orElseThrow(() -> new AppException(CartOrderErrorCode.CART_ITEM_NOT_FOUND));

            // Nếu số lượng còn 1 thì xóa luôn sản phẩm khỏi giỏ hàng
            if (cartItem.getQuantity() == 1) {
                cart.getCartItems().remove(cartItem);
                cartItemRepository.delete(cartItem);
            } else {
                cartItem.setQuantity(cartItem.getQuantity() - 1);
                cartItem.setSubtotal(cartItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
                cartItemRepository.save(cartItem);
            }
        }

        updateCartTotal(cart);
        return cartMapper.toCartResponse(cart);
    }

    @Override
    @Transactional
    public void clearCart() {
        User user = getAuthenticatedUser();
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(CartOrderErrorCode.CART_NOT_FOUND));

        cartItemRepository.deleteAllByCartId(cart.getId());
        cart.setTotalPrice(BigDecimal.ZERO);
        cartRepository.save(cart);
    }

    @Override
    public BigDecimal getTotalCartAmount() {
        User user = getAuthenticatedUser();
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(CartOrderErrorCode.CART_NOT_FOUND));

        return cart.getTotalPrice();
    }

    private void updateCartTotal(Cart cart) {
        BigDecimal newTotal = cart.getCartItems().stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (!newTotal.equals(cart.getTotalPrice())) {
            cart.setTotalPrice(newTotal);
            cartRepository.save(cart);
        }
    }

    private User getAuthenticatedUser() {
        String username = SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new AppException(CommonErrorCode.UNAUTHENTICATED));

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(UserErrorCode.USER_NOT_FOUND));
    }

    private void validateSizeOption(Product product, String sizeName) {
        log.info("🔍 Validating size '{}' for product ID: {} (Category: {})", sizeName, product.getId(), product.getCategory().getName());
        List<String> validSizes = sizeCategoryRepository.findSizeNamesByCategoryId(product.getCategory().getId());
        log.info("📌 Valid sizes for category: {}", validSizes);

        boolean isValid = validSizes.stream()
                .anyMatch(s -> s.equalsIgnoreCase(sizeName));

        if (!isValid) {
            log.error("❌ Invalid size '{}' for product ID: {}. Expected one of: {}", sizeName, product.getId(), validSizes);
            throw new AppException(ProductErrorCode.INVALID_PRODUCT_OPTION);
        }

        log.info("✅ Size validated: {}", sizeName);
    }
    private String generateKey(Long productId, String sizeName, String color) {
        return productId + "-" + sizeName + "-" + color;
    }
}


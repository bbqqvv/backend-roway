package org.bbqqvv.backendecommerce.service.impl;

import org.bbqqvv.backendecommerce.config.jwt.SecurityUtils;
import org.bbqqvv.backendecommerce.dto.request.CartItemRequest;
import org.bbqqvv.backendecommerce.dto.request.CartRequest;
import org.bbqqvv.backendecommerce.dto.response.CartResponse;
import org.bbqqvv.backendecommerce.entity.*;
import org.bbqqvv.backendecommerce.exception.AppException;
import org.bbqqvv.backendecommerce.exception.codes.ProductErrorCode;
import org.bbqqvv.backendecommerce.mapper.CartMapper;
import org.bbqqvv.backendecommerce.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private SizeProductRepository sizeProductRepository;
    @Mock private SizeCategoryRepository sizeCategoryRepository;
    @Mock private UserRepository userRepository;
    @Mock private CartMapper cartMapper;

    @InjectMocks
    private CartServiceImpl cartService;

    private User user;
    private Cart cart;
    private Product product;
    private SizeProduct sizeProduct;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("testuser").build();
        cart = Cart.builder().id(1L).user(user).cartItems(new ArrayList<>()).totalPrice(BigDecimal.ZERO).build();
        
        Category category = Category.builder().id(1L).name("Shirts").build();
        product = Product.builder().id(1L).name("T-Shirt").category(category).build();
        
        sizeProduct = SizeProduct.builder()
                .sizeName("L")
                .price(BigDecimal.valueOf(100))
                .priceAfterDiscount(BigDecimal.valueOf(100))
                .build();
    }

    @Test
    @DisplayName("Thêm sản phẩm vào giỏ hàng thành công")
    void addOrUpdateProductInCart_shouldReturnCartResponse() {
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            // Arrange
            mockedSecurity.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("testuser"));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(sizeCategoryRepository.findSizeNamesByCategoryId(1L)).thenReturn(List.of("L", "M"));
            when(sizeProductRepository.findByProductIdAndSizeName(1L, "L")).thenReturn(Optional.of(sizeProduct));

            // Custom handle for SizeProductVariant link
            SizeProductVariant spv = SizeProductVariant.builder()
                    .stock(10)
                    .productVariant(ProductVariant.builder().color("Red").build())
                    .build();
            sizeProduct.setProductVariantSizes(List.of(spv));

            CartRequest request = new CartRequest();
            CartItemRequest item = new CartItemRequest();
            item.setProductId(1L);
            item.setSizeName("L");
            item.setColor("Red");
            item.setQuantity(2);
            request.setItems(List.of(item));

            when(cartMapper.toCartResponse(any())).thenReturn(new CartResponse());

            // Act
            CartResponse response = cartService.addOrUpdateProductInCart(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(cart.getCartItems()).hasSize(1);
            assertThat(cart.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(200));
        }
    }

    @Test
    @DisplayName("Thêm vào giỏ hàng thất bại - Hết hàng")
    void addOrUpdateProductInCart_shouldThrowException_whenOutOfStock() {
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            // Arrange
            mockedSecurity.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("testuser"));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(sizeCategoryRepository.findSizeNamesByCategoryId(1L)).thenReturn(List.of("L"));
            when(sizeProductRepository.findByProductIdAndSizeName(1L, "L")).thenReturn(Optional.of(sizeProduct));

            SizeProductVariant spv = SizeProductVariant.builder()
                    .stock(1) // Only 1 in stock
                    .productVariant(ProductVariant.builder().color("Red").build())
                    .build();
            sizeProduct.setProductVariantSizes(List.of(spv));

            CartRequest request = new CartRequest();
            CartItemRequest item = new CartItemRequest();
            item.setProductId(1L);
            item.setSizeName("L");
            item.setColor("Red");
            item.setQuantity(2); // Buying 2
            request.setItems(List.of(item));

            // Act & Assert
            assertThatThrownBy(() -> cartService.addOrUpdateProductInCart(request))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ProductErrorCode.OUT_OF_STOCK);
        }
    }
}

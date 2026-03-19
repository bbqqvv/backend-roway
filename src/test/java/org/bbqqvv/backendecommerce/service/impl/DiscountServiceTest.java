package org.bbqqvv.backendecommerce.service.impl;

import org.bbqqvv.backendecommerce.config.jwt.SecurityUtils;
import org.bbqqvv.backendecommerce.dto.request.DiscountPreviewRequest;
import org.bbqqvv.backendecommerce.dto.request.DiscountRequest;
import org.bbqqvv.backendecommerce.dto.response.DiscountPreviewResponse;
import org.bbqqvv.backendecommerce.dto.response.DiscountResponse;
import org.bbqqvv.backendecommerce.entity.*;
import org.bbqqvv.backendecommerce.exception.AppException;
import org.bbqqvv.backendecommerce.exception.codes.SocialMarketingErrorCode;
import org.bbqqvv.backendecommerce.mapper.DiscountMapper;
import org.bbqqvv.backendecommerce.repository.*;
import org.bbqqvv.backendecommerce.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscountServiceTest {

    @Mock private DiscountRepository discountRepository;
    @Mock private DiscountMapper discountMapper;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @Mock private DiscountProductRepository discountProductRepository;
    @Mock private DiscountUserRepository discountUserRepository;
    @Mock private CartRepository cartRepository;

    @InjectMocks
    private DiscountServiceImpl discountService;

    private Discount discount;
    private DiscountRequest discountRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).username("testuser").build();

        discount = Discount.builder()
                .id(1L)
                .code("SAVE10")
                .discountAmount(BigDecimal.valueOf(10))
                .discountType(DiscountType.PERCENTAGE)
                .minOrderValue(BigDecimal.valueOf(100))
                .maxDiscountAmount(BigDecimal.valueOf(50))
                .usageLimit(100)
                .timesUsed(0)
                .startDate(LocalDateTime.now().minusDays(1))
                .expiryDate(LocalDateTime.now().plusDays(10))
                .active(true)
                .applicableProducts(new ArrayList<>())
                .applicableUsers(new ArrayList<>())
                .build();

        discountRequest = DiscountRequest.builder()
                .code("SAVE10")
                .discountAmount(BigDecimal.valueOf(10))
                .maxDiscountAmount(BigDecimal.valueOf(50))
                .discountType(DiscountType.PERCENTAGE)
                .minOrderValue(BigDecimal.valueOf(100))
                .usageLimit(100)
                .startDate(LocalDateTime.now().minusDays(1))
                .expiryDate(LocalDateTime.now().plusDays(10))
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Tạo mã giảm giá thành công")
    void createDiscount_shouldReturnDiscountResponse_whenValidRequest() {
        // Arrange
        when(discountRepository.existsByCode("SAVE10")).thenReturn(false);
        when(discountRepository.saveAndFlush(any(Discount.class))).thenReturn(discount);
        when(discountRepository.save(any(Discount.class))).thenReturn(discount);
        when(discountMapper.toDiscountResponse(any(Discount.class))).thenReturn(new DiscountResponse());

        // Act
        DiscountResponse response = discountService.createDiscount(discountRequest);

        // Assert
        assertThat(response).isNotNull();
        verify(discountRepository).save(any(Discount.class));
    }

    @Test
    @DisplayName("Tạo mã giảm giá thất bại - Trùng mã")
    void createDiscount_shouldThrowException_whenCodeExists() {
        // Arrange
        when(discountRepository.existsByCode("SAVE10")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> discountService.createDiscount(discountRequest))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", SocialMarketingErrorCode.DUPLICATE_DISCOUNT_CODE);
    }

    @Test
    @DisplayName("Preview mã giảm giá thành công - Global")
    void previewDiscount_shouldReturnValidResponse_whenGlobalDiscount() {
        // Arrange
        DiscountPreviewRequest request = DiscountPreviewRequest.builder().discountCode("SAVE10").build();
        
        Product product = Product.builder().id(101L).build();
        CartItem item = CartItem.builder()
                .product(product)
                .quantity(1)
                .price(BigDecimal.valueOf(200)) // Cần set price vì getSubtotal() dùng nó
                .subtotal(BigDecimal.valueOf(200))
                .build();
        Cart cart = Cart.builder()
                .totalPrice(BigDecimal.valueOf(200))
                .cartItems(List.of(item))
                .build();
        
        try (var mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("testuser"));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(discountRepository.findByCode("SAVE10")).thenReturn(Optional.of(discount));
            when(cartRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(cart));

            // Act
            DiscountPreviewResponse response = discountService.previewDiscount(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getValid()).isTrue();
            assertThat(response.getDiscountAmount()).isEqualByComparingTo(BigDecimal.valueOf(20));
        }
    }

    @Test
    @DisplayName("calculateDiscountAmount - Target sản phẩm cụ thể")
    void calculateDiscountAmount_shouldFilterProducts_whenTargeted() {
        // Arrange
        Product p1 = Product.builder().id(101L).build();
        Product p2 = Product.builder().id(102L).build();
        
        DiscountProduct dp1 = new DiscountProduct(); dp1.setProduct(p1);
        discount.setApplicableProducts(List.of(dp1));

        List<Long> productIds = List.of(101L, 102L);
        List<BigDecimal> subtotals = List.of(BigDecimal.valueOf(100), BigDecimal.valueOf(200));
        BigDecimal totalAmount = BigDecimal.valueOf(300);

        // Act
        BigDecimal result = discountService.calculateDiscountAmount(discount, productIds, subtotals, totalAmount);

        // Assert
        // Chỉ tính cho p1 (100k) * 10% = 10k
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(10));
    }

    @Test
    @DisplayName("calculateDiscountAmount - Giới hạn số tiền giảm tối đa (Max Discount)")
    void calculateDiscountAmount_shouldCapAtMaxAmount() {
        // Arrange
        discount.setMaxDiscountAmount(BigDecimal.valueOf(5)); // Giảm tối đa 5k
        
        List<Long> productIds = List.of(101L);
        List<BigDecimal> subtotals = List.of(BigDecimal.valueOf(100)); // 10% của 100 là 10k
        BigDecimal totalAmount = BigDecimal.valueOf(100);

        // Act
        BigDecimal result = discountService.calculateDiscountAmount(discount, productIds, subtotals, totalAmount);

        // Assert
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(5));
    }

    @Test
    @DisplayName("getDiscountByCode - Tìm thấy mã")
    void getDiscountByCode_shouldReturnDiscount() {
        when(discountRepository.findByCode("SAVE10")).thenReturn(Optional.of(discount));
        Discount result = discountService.getDiscountByCode("SAVE10");
        assertThat(result).isEqualTo(discount);
    }

    @Test
    @DisplayName("Preview mã giảm giá thất bại - Hết hạn")
    void previewDiscount_shouldReturnInvalidResponse_whenDiscountExpired() {
        // Arrange
        discount.setExpiryDate(LocalDateTime.now().minusDays(1));
        DiscountPreviewRequest request = DiscountPreviewRequest.builder().discountCode("SAVE10").build();
        when(discountRepository.findByCode("SAVE10")).thenReturn(Optional.of(discount));
        
        Cart cart = Cart.builder().totalPrice(BigDecimal.valueOf(200)).cartItems(new ArrayList<>()).build();
        try (var mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("testuser"));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(cartRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(cart));

            // Act
            DiscountPreviewResponse response = discountService.previewDiscount(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getValid()).isFalse();
            assertThat(response.getDiscountAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }
}

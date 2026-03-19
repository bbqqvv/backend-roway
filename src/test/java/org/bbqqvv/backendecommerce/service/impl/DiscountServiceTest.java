package org.bbqqvv.backendecommerce.service.impl;

import org.bbqqvv.backendecommerce.dto.request.DiscountPreviewRequest;
import org.bbqqvv.backendecommerce.dto.request.DiscountRequest;
import org.bbqqvv.backendecommerce.dto.response.DiscountPreviewResponse;
import org.bbqqvv.backendecommerce.dto.response.DiscountResponse;
import org.bbqqvv.backendecommerce.entity.Discount;
import org.bbqqvv.backendecommerce.entity.DiscountType;
import org.bbqqvv.backendecommerce.exception.AppException;
import org.bbqqvv.backendecommerce.exception.codes.SocialMarketingErrorCode;
import org.bbqqvv.backendecommerce.mapper.DiscountMapper;
import org.bbqqvv.backendecommerce.repository.DiscountProductRepository;
import org.bbqqvv.backendecommerce.repository.DiscountRepository;
import org.bbqqvv.backendecommerce.repository.DiscountUserRepository;
import org.bbqqvv.backendecommerce.repository.ProductRepository;
import org.bbqqvv.backendecommerce.repository.UserRepository;
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
import java.util.Collections;
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
    @Mock private CartService cartService;

    @InjectMocks
    private DiscountServiceImpl discountService;

    private Discount discount;
    private DiscountRequest discountRequest;

    @BeforeEach
    void setUp() {
        discount = Discount.builder()
                .id(1L)
                .code("SAVE10")
                .discountAmount(BigDecimal.valueOf(10))
                .discountType(DiscountType.PERCENTAGE)
                .minOrderValue(BigDecimal.valueOf(100))
                .usageLimit(100)
                .startDate(LocalDateTime.now().minusDays(1))
                .expiryDate(LocalDateTime.now().plusDays(10))
                .active(true)
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
                .applicableProducts(Collections.emptyList())
                .applicableUsers(Collections.emptyList())
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
    @DisplayName("Preview mã giảm giá thành công")
    void previewDiscount_shouldReturnValidResponse_whenDiscountApplicable() {
        // Arrange
        DiscountPreviewRequest request = DiscountPreviewRequest.builder().discountCode("SAVE10").build();
        when(discountRepository.findByCode("SAVE10")).thenReturn(Optional.of(discount));
        when(cartService.getTotalCartAmount()).thenReturn(BigDecimal.valueOf(200));

        // Act
        DiscountPreviewResponse response = discountService.previewDiscount(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getValid()).isTrue();
        assertThat(response.getDiscountAmount()).isEqualByComparingTo(BigDecimal.valueOf(20)); // 10% of 200
        assertThat(response.getFinalAmount()).isEqualByComparingTo(BigDecimal.valueOf(180));
    }

    @Test
    @DisplayName("Preview mã giảm giá thất bại - Hết hạn")
    void previewDiscount_shouldReturnInvalidResponse_whenDiscountExpired() {
        // Arrange
        discount.setExpiryDate(LocalDateTime.now().minusDays(1));
        DiscountPreviewRequest request = DiscountPreviewRequest.builder().discountCode("SAVE10").build();
        when(discountRepository.findByCode("SAVE10")).thenReturn(Optional.of(discount));
        when(cartService.getTotalCartAmount()).thenReturn(BigDecimal.valueOf(200));

        // Act
        DiscountPreviewResponse response = discountService.previewDiscount(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getValid()).isFalse();
        assertThat(response.getDiscountAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}

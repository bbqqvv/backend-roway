package org.bbqqvv.backendecommerce.service.impl;

import org.bbqqvv.backendecommerce.config.jwt.SecurityUtils;
import org.bbqqvv.backendecommerce.dto.PageResponse;
import org.bbqqvv.backendecommerce.dto.request.ProductReviewRequest;
import org.bbqqvv.backendecommerce.dto.response.ProductReviewResponse;
import org.bbqqvv.backendecommerce.entity.*;
import org.bbqqvv.backendecommerce.exception.AppException;
import org.bbqqvv.backendecommerce.exception.codes.CartOrderErrorCode;
import org.bbqqvv.backendecommerce.exception.codes.CommonErrorCode;
import org.bbqqvv.backendecommerce.exception.codes.SocialMarketingErrorCode;
import org.bbqqvv.backendecommerce.mapper.ProductReviewMapper;
import org.bbqqvv.backendecommerce.repository.*;
import org.bbqqvv.backendecommerce.service.img.CloudinaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductReviewServiceTest {

    @Mock private ProductReviewRepository productReviewRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductReviewMapper productReviewMapper;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private CloudinaryService cloudinaryService;

    @InjectMocks
    private ProductReviewServiceImpl productReviewService;

    private User user;
    private Product product;
    private OrderItem orderItem;
    private ProductReview review;
    private ProductReviewRequest reviewRequest;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("testuser").build();
        product = Product.builder().id(101L).name("Roway T-Shirt").build();
        Order order = Order.builder()
                .id(10L)
                .user(user)
                .status(OrderStatus.DELIVERED)
                .build();
        orderItem = OrderItem.builder()
                .id(50L)
                .product(product)
                .order(order)
                .build();
        
        review = new ProductReview();
        review.setId(1L);
        review.setUser(user);
        review.setProduct(product);
        review.setOrderItem(orderItem);
        review.setRating(5);
        review.setReviewText("Great product!");
        review.setImages(new ArrayList<>());
        review.setCreatedAt(LocalDateTime.now());

        reviewRequest = new ProductReviewRequest();
        reviewRequest.setProductId(101L);
        reviewRequest.setOrderItemId(50L);
        reviewRequest.setRating(5);
        reviewRequest.setReviewText("Great product!");
    }

    @Test
    @DisplayName("Thêm đánh giá mới thành công")
    void addOrUpdateReview_shouldAddNew_whenNoExistingReview() {
        try (var mockedSecurity = mockStatic(SecurityUtils.class)) {
            // Arrange
            mockedSecurity.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("testuser"));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(orderItemRepository.findById(50L)).thenReturn(Optional.of(orderItem));
            when(productReviewRepository.findByOrderItemId(50L)).thenReturn(Optional.empty());
            when(productReviewRepository.save(any())).thenReturn(review);
            when(productReviewMapper.toResponse(any())).thenReturn(new ProductReviewResponse());

            // Act
            productReviewService.addOrUpdateReview(reviewRequest);

            // Assert
            verify(productReviewRepository).save(any(ProductReview.class));
        }
    }

    @Test
    @DisplayName("Cập nhật đánh giá thành công")
    void addOrUpdateReview_shouldUpdate_whenExistingReviewWithin30Days() {
        try (var mockedSecurity = mockStatic(SecurityUtils.class)) {
            // Arrange
            mockedSecurity.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("testuser"));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(orderItemRepository.findById(50L)).thenReturn(Optional.of(orderItem));
            when(productReviewRepository.findByOrderItemId(50L)).thenReturn(Optional.of(review));
            when(productReviewRepository.save(any())).thenReturn(review);
            when(productReviewMapper.toResponse(any())).thenReturn(new ProductReviewResponse());

            // Act
            productReviewService.addOrUpdateReview(reviewRequest);

            // Assert
            verify(productReviewRepository).save(review);
        }
    }

    @Test
    @DisplayName("Cập nhật đánh giá thất bại - Quá hạn 30 ngày")
    void addOrUpdateReview_shouldThrowException_whenReviewOlderThan30Days() {
        review.setCreatedAt(LocalDateTime.now().minusDays(31));
        try (var mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("testuser"));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(orderItemRepository.findById(50L)).thenReturn(Optional.of(orderItem));
            when(productReviewRepository.findByOrderItemId(50L)).thenReturn(Optional.of(review));

            assertThatThrownBy(() -> productReviewService.addOrUpdateReview(reviewRequest))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", SocialMarketingErrorCode.REVIEW_EDIT_EXPIRED);
        }
    }

    @Test
    @DisplayName("Thêm đánh giá thất bại - Chưa nhận hàng")
    void addOrUpdateReview_shouldThrowException_whenNoDeliveredOrder() {
        try (var mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("testuser"));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            
            // Set order status to PENDING instead of DELIVERED
            orderItem.getOrder().setStatus(OrderStatus.PENDING);
            when(orderItemRepository.findById(50L)).thenReturn(Optional.of(orderItem));

            assertThatThrownBy(() -> productReviewService.addOrUpdateReview(reviewRequest))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", CartOrderErrorCode.ORDER_NOT_COMPLETED);
        }
    }

    @Test
    @DisplayName("Lấy đánh giá theo sản phẩm")
    void getReviewsByProduct_shouldReturnPageResponse() {
        try (var mockedSecurity = mockStatic(SecurityUtils.class)) {
            // Arrange
            mockedSecurity.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.empty());
            Pageable pageable = PageRequest.of(0, 10);
            when(productRepository.findById(101L)).thenReturn(Optional.of(product));
            when(productReviewRepository.findByProductId(eq(101L), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(review)));
            when(productReviewMapper.toResponse(any())).thenReturn(new ProductReviewResponse());

            // Act
            PageResponse<ProductReviewResponse> response = productReviewService.getReviewsByProduct(101L, pageable);

            // Assert
            assertThat(response.items()).hasSize(1);
        }
    }

    @Test
    @DisplayName("Xóa đánh giá thành công - Owner")
    void deleteReview_shouldDelete_whenRequesterIsOwner() {
        try (var mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("testuser"));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(productReviewRepository.findById(1L)).thenReturn(Optional.of(review));

            productReviewService.deleteReview(1L);

            verify(productReviewRepository).delete(review);
        }
    }

    @Test
    @DisplayName("Xóa đánh giá thất bại - Không phải chủ sở hữu")
    void deleteReview_shouldThrowException_whenNotOwner() {
        User otherUser = User.builder().id(99L).username("other").build();
        try (var mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("testuser"));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            
            review.setUser(otherUser);
            when(productReviewRepository.findById(1L)).thenReturn(Optional.of(review));

            assertThatThrownBy(() -> productReviewService.deleteReview(1L))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", CommonErrorCode.ACCESS_DENIED);
        }
    }
}

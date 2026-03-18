package org.bbqqvv.backendecommerce.service.impl;

import org.bbqqvv.backendecommerce.exception.codes.*;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.bbqqvv.backendecommerce.config.jwt.SecurityUtils;
import org.bbqqvv.backendecommerce.dto.PageResponse;
import org.bbqqvv.backendecommerce.dto.request.ProductReviewRequest;
import org.bbqqvv.backendecommerce.dto.response.ProductReviewResponse;
import org.bbqqvv.backendecommerce.entity.*;
import org.bbqqvv.backendecommerce.exception.AppException;
import org.bbqqvv.backendecommerce.exception.ErrorCode;
import org.bbqqvv.backendecommerce.mapper.ProductReviewMapper;
import org.bbqqvv.backendecommerce.repository.OrderItemRepository;
import org.bbqqvv.backendecommerce.repository.ProductRepository;
import org.bbqqvv.backendecommerce.repository.ProductReviewRepository;
import org.bbqqvv.backendecommerce.repository.UserRepository;
import org.bbqqvv.backendecommerce.service.ProductReviewService;
import org.bbqqvv.backendecommerce.service.img.CloudinaryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.bbqqvv.backendecommerce.util.PagingUtil.toPageResponse;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductReviewServiceImpl implements ProductReviewService {

    ProductReviewRepository productReviewRepository;
    ProductRepository productRepository;
    UserRepository userRepository;
    ProductReviewMapper productReviewMapper;
    OrderItemRepository orderItemRepository;
    CloudinaryService cloudinaryService;

    public ProductReviewServiceImpl(
            ProductReviewRepository productReviewRepository,
            ProductRepository productRepository,
            ProductReviewMapper productReviewMapper,
            UserRepository userRepository,
            OrderItemRepository orderItemRepository,
            CloudinaryService cloudinaryService
    ) {
        this.productReviewRepository = productReviewRepository;
        this.productRepository = productRepository;
        this.productReviewMapper = productReviewMapper;
        this.userRepository = userRepository;
        this.orderItemRepository = orderItemRepository;
        this.cloudinaryService = cloudinaryService;
    }

    private User getAuthenticatedUser() {
        return SecurityUtils.getCurrentUserLogin()
                .flatMap(userRepository::findByUsername)
                .orElseThrow(() -> new AppException(CommonErrorCode.UNAUTHENTICATED));
    }

    @Override
    @Transactional
    public ProductReviewResponse addOrUpdateReview(ProductReviewRequest reviewRequest) {
        User user = getAuthenticatedUser();

        List<OrderItem> deliveredOrderItems = orderItemRepository
                .findByProductIdAndOrderUserIdAndOrderStatus(
                        reviewRequest.getProductId(),
                        user.getId(),
                        OrderStatus.DELIVERED
                );

        if (deliveredOrderItems.isEmpty()) {
            throw new AppException(CartOrderErrorCode.ORDER_NOT_COMPLETED);
        }

        OrderItem orderItem = deliveredOrderItems.get(0);
        Product product = orderItem.getProduct();

        ProductReview existingReview = productReviewRepository.findByOrderItemId(orderItem.getId()).orElse(null);

        if (existingReview != null) {
            if (!existingReview.getUser().getId().equals(user.getId())) {
                throw new AppException(CommonErrorCode.ACCESS_DENIED);
            }

            if (existingReview.getCreatedAt().plusDays(30).isBefore(LocalDateTime.now())) {
                throw new AppException(SocialMarketingErrorCode.REVIEW_EDIT_EXPIRED);
            }

            existingReview.setRating(reviewRequest.getRating());
            existingReview.setReviewText(reviewRequest.getReviewText());

            if (reviewRequest.getImageFiles() != null && !reviewRequest.getImageFiles().isEmpty()) {
                List<String> imageUrls = cloudinaryService.uploadImages(reviewRequest.getImageFiles());

                List<ProductReviewImage> reviewImages = imageUrls.stream()
                        .map(url -> ProductReviewImage.builder()
                                .productReview(existingReview)
                                .imageUrl(url)
                                .build())
                        .toList();

                existingReview.getImages().clear();
                existingReview.getImages().addAll(reviewImages);
            }

            return productReviewMapper.toResponse(productReviewRepository.save(existingReview));
        }

        // New review
        ProductReview newReview = new ProductReview();
        newReview.setUser(user);
        newReview.setProduct(product);
        newReview.setOrderItem(orderItem);
        newReview.setRating(reviewRequest.getRating());
        newReview.setReviewText(reviewRequest.getReviewText());

        if (reviewRequest.getImageFiles() != null && !reviewRequest.getImageFiles().isEmpty()) {
            List<String> imageUrls = cloudinaryService.uploadImages(reviewRequest.getImageFiles());

            List<ProductReviewImage> reviewImages = imageUrls.stream()
                    .map(url -> ProductReviewImage.builder()
                            .productReview(newReview)
                            .imageUrl(url)
                            .build())
                    .toList();

            newReview.setImages(reviewImages);
        }

        return productReviewMapper.toResponse(productReviewRepository.save(newReview));
    }

    @Override
    public PageResponse<ProductReviewResponse> getReviewsByProduct(Long productId, Pageable pageable) {
        productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ProductErrorCode.PRODUCT_NOT_FOUND));
        Page<ProductReview> reviewPage = productReviewRepository.findByProductId(productId, pageable);
        return toPageResponse(reviewPage, productReviewMapper::toResponse);
    }

    @Override
    public PageResponse<ProductReviewResponse> getReviewsByUser(Pageable pageable) {
        User user = getAuthenticatedUser();
        Page<ProductReview> reviewPage = productReviewRepository.findByUserId(user.getId(), pageable);
        return toPageResponse(reviewPage, productReviewMapper::toResponse);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        User user = getAuthenticatedUser();
        ProductReview review = productReviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(SocialMarketingErrorCode.REVIEW_NOT_FOUND));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new AppException(CommonErrorCode.ACCESS_DENIED);
        }

        productReviewRepository.delete(review);
    }
}


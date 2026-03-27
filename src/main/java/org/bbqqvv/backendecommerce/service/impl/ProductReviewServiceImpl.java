package org.bbqqvv.backendecommerce.service.impl;

import org.bbqqvv.backendecommerce.exception.codes.*;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.bbqqvv.backendecommerce.config.jwt.SecurityUtils;
import org.bbqqvv.backendecommerce.dto.PageResponse;
import org.bbqqvv.backendecommerce.dto.request.ImageMetadata;
import org.bbqqvv.backendecommerce.dto.request.ProductReviewRequest;
import org.bbqqvv.backendecommerce.dto.response.ProductReviewResponse;
import org.bbqqvv.backendecommerce.entity.*;
import org.bbqqvv.backendecommerce.exception.AppException;
import org.bbqqvv.backendecommerce.mapper.ProductReviewMapper;
import org.bbqqvv.backendecommerce.repository.OrderItemRepository;
import org.bbqqvv.backendecommerce.repository.ProductRepository;
import org.bbqqvv.backendecommerce.repository.ProductReviewRepository;
import org.bbqqvv.backendecommerce.repository.UserRepository;
import org.bbqqvv.backendecommerce.service.ProductReviewService;
import org.bbqqvv.backendecommerce.service.img.CloudinaryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.bbqqvv.backendecommerce.dto.response.ReviewStatsResponse;

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

        OrderItem orderItem = orderItemRepository.findById(reviewRequest.getOrderItemId())
                .orElseThrow(() -> new RuntimeException("OrderItem không tồn tại."));

        if (!orderItem.getOrder().getUser().getId().equals(user.getId()) || orderItem.getOrder().getStatus() != OrderStatus.DELIVERED) {
            throw new AppException(CartOrderErrorCode.ORDER_NOT_COMPLETED);
        }

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
            existingReview.setAnonymous(reviewRequest.isAnonymous());

            if (reviewRequest.getImageFiles() != null && !reviewRequest.getImageFiles().isEmpty()) {
                List<String> imageUrls = cloudinaryService.uploadImages(reviewRequest.getImageFiles())
                        .stream().map(ImageMetadata::getUrl).toList();

                List<ProductReviewImage> reviewImages = imageUrls.stream()
                        .map(url -> ProductReviewImage.builder()
                                .productReview(existingReview)
                                .imageUrl(url)
                                .build())
                        .toList();

                existingReview.getImages().clear();
                existingReview.getImages().addAll(reviewImages);
            }

            return toResponseWithLikeStatus(productReviewRepository.save(existingReview));
        }

        // New review
        ProductReview newReview = new ProductReview();
        newReview.setUser(user);
        newReview.setProduct(product);
        newReview.setOrderItem(orderItem);
        newReview.setRating(reviewRequest.getRating());
        newReview.setReviewText(reviewRequest.getReviewText());
        newReview.setAnonymous(reviewRequest.isAnonymous());

        if (reviewRequest.getImageFiles() != null && !reviewRequest.getImageFiles().isEmpty()) {
            List<String> imageUrls = cloudinaryService.uploadImages(reviewRequest.getImageFiles())
                    .stream().map(ImageMetadata::getUrl).toList();

            List<ProductReviewImage> reviewImages = imageUrls.stream()
                    .map(url -> ProductReviewImage.builder()
                            .productReview(newReview)
                            .imageUrl(url)
                            .build())
                    .toList();

            newReview.setImages(reviewImages);
        }

        return toResponseWithLikeStatus(productReviewRepository.save(newReview));
    }

    @Override
    public PageResponse<ProductReviewResponse> getReviewsByProduct(Long productId, Pageable pageable) {
        return getReviewsByProduct(productId, null, pageable);
    }

    @Override
    public PageResponse<ProductReviewResponse> getReviewsByProduct(Long productId, Integer rating, Pageable pageable) {
        productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ProductErrorCode.PRODUCT_NOT_FOUND));
        Page<ProductReview> reviewPage;
        if (rating != null && rating >= 1 && rating <= 5) {
            reviewPage = productReviewRepository.findByProductIdAndRating(productId, rating, pageable);
        } else {
            reviewPage = productReviewRepository.findByProductId(productId, pageable);
        }
        return toPageResponse(reviewPage, this::toResponseWithLikeStatus);
    }

    @Override
    public ReviewStatsResponse getReviewStats(Long productId) {
        productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ProductErrorCode.PRODUCT_NOT_FOUND));

        long total = productReviewRepository.countByProductId(productId);
        double avg = total > 0 ? productReviewRepository.averageRatingByProductId(productId) : 0.0;

        java.util.Map<Integer, Long> distribution = new java.util.LinkedHashMap<>();
        for (int star = 5; star >= 1; star--) {
            distribution.put(star, productReviewRepository.countByProductIdAndRating(productId, star));
        }

        return ReviewStatsResponse.builder()
                .averageRating(Math.round(avg * 10.0) / 10.0)
                .totalReviews(total)
                .distribution(distribution)
                .build();
    }

    @Override
    public PageResponse<ProductReviewResponse> getReviewsByUser(Pageable pageable) {
        User user = getAuthenticatedUser();
        Page<ProductReview> reviewPage = productReviewRepository.findByUserId(user.getId(), pageable);
        return toPageResponse(reviewPage, this::toResponseWithLikeStatus);
    }

    @Override
    public PageResponse<ProductReviewResponse> getAllReviews(Pageable pageable) {
        Page<ProductReview> reviewPage = productReviewRepository.findAll(pageable);
        return toPageResponse(reviewPage, this::toResponseWithLikeStatus);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        User user = getAuthenticatedUser();
        ProductReview review = productReviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(SocialMarketingErrorCode.REVIEW_NOT_FOUND));

        // 🛡️ Security Check: Chỉ chủ nhân hoặc ADMIN mới được xóa
        if (!review.getUser().getId().equals(user.getId()) && !user.isAdmin()) {
            log.warn("User {} tried to delete review {} without permission", user.getUsername(), reviewId);
            throw new AppException(CommonErrorCode.ACCESS_DENIED);
        }

        productReviewRepository.delete(review);
    }

    @Override
    @Transactional
    public ProductReviewResponse toggleLike(Long reviewId) {
        User user = getAuthenticatedUser();
        ProductReview review = productReviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(SocialMarketingErrorCode.REVIEW_NOT_FOUND));

        if (review.getLikedUserIds().contains(user.getId())) {
            review.getLikedUserIds().remove(user.getId());
        } else {
            review.getLikedUserIds().add(user.getId());
        }

        review = productReviewRepository.save(review);
        return toResponseWithLikeStatus(review);
    }

    private ProductReviewResponse toResponseWithLikeStatus(ProductReview review) {
        ProductReviewResponse response = productReviewMapper.toResponse(review);
        Long currentUserId = SecurityUtils.getCurrentUserLogin()
                .flatMap(userRepository::findByUsername)
                .map(User::getId)
                .orElse(null);
        if (currentUserId != null && review.getLikedUserIds() != null) {
            response.setLikedByCurrentUser(review.getLikedUserIds().contains(currentUserId));
        } else {
            response.setLikedByCurrentUser(false);
        }
        return response;
    }
}


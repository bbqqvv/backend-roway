package org.bbqqvv.backendecommerce.service.impl;

import org.bbqqvv.backendecommerce.exception.codes.*;

import lombok.RequiredArgsConstructor;
import org.bbqqvv.backendecommerce.config.jwt.SecurityUtils;
import org.bbqqvv.backendecommerce.dto.PageResponse;
import org.bbqqvv.backendecommerce.dto.response.ProductResponse;
import org.bbqqvv.backendecommerce.entity.Product;
import org.bbqqvv.backendecommerce.entity.RecentlyViewedProduct;
import org.bbqqvv.backendecommerce.entity.User;
import org.bbqqvv.backendecommerce.exception.AppException;
import org.bbqqvv.backendecommerce.exception.ErrorCode;
import org.bbqqvv.backendecommerce.mapper.ProductMapper;
import org.bbqqvv.backendecommerce.repository.ProductRepository;
import org.bbqqvv.backendecommerce.repository.RecentlyViewedProductRepository;
import org.bbqqvv.backendecommerce.repository.UserRepository;
import org.bbqqvv.backendecommerce.service.RecentlyViewedProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecentlyViewedProductServiceImpl implements RecentlyViewedProductService {

    private final RecentlyViewedProductRepository repository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
private final UserRepository userRepository;
    // Hàm này sử dụng getAuthenticatedUser để lấy thông tin người dùng
    private User getAuthenticatedUser() {
        String username = SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new AppException(CommonErrorCode.UNAUTHENTICATED));

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(UserErrorCode.USER_NOT_FOUND));
    }


    private static final int MAX_ITEMS = 20;

    @Override
    @org.springframework.scheduling.annotation.Async
    @org.springframework.transaction.annotation.Transactional
    public void markProductAsViewed(Long productId) {
        log.info("Marking product {} as viewed asynchronously", productId);
        try {
            // Lấy thông tin người dùng hiện tại
            User currentUser = getAuthenticatedUser();

            // Kiểm tra xem sản phẩm có tồn tại không
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            // Kiểm tra xem sản phẩm đã được đánh dấu là đã xem chưa
            RecentlyViewedProduct existing = repository.findTop1ByUserAndProductOrderByUpdatedAtDesc(currentUser, product);

            if (existing != null) {
                // JPA Auditing updates updatedAt. Just saving will trigger it.
                // We update dummy field or just call save to ensure updatedAt is changed.
                repository.save(existing);
            } else {
                // Trước khi thêm mới, kiểm tra số lượng hiện tại
                long count = repository.countByUser(currentUser);
                if (count >= MAX_ITEMS) {
                    // Xóa bản ghi cũ nhất (1 cái) để nhường chỗ
                    repository.deleteOldestByUserId(currentUser.getId(), 1);
                }

                RecentlyViewedProduct recentlyViewedProduct = RecentlyViewedProduct.builder()
                        .user(currentUser)
                        .product(product)
                        .build();
                repository.save(recentlyViewedProduct);
            }
        } catch (Exception e) {
            log.error("Failed to mark product {} as viewed: {}", productId, e.getMessage());
        }
    }

    @Override
    public PageResponse<ProductResponse> getRecentlyViewedProducts(Pageable pageable) {
        // Lấy thông tin người dùng hiện tại
        User currentUser = getAuthenticatedUser();

        // Truy vấn danh sách sản phẩm đã xem với phân trang
        Page<RecentlyViewedProduct> pageResult = repository.findByUserOrderByUpdatedAtDesc(currentUser, pageable);

        // Chuyển đổi danh sách sản phẩm đã xem sang dạng ProductResponse
        List<ProductResponse> productResponses = pageResult.getContent().stream()
                .map(recentlyViewedProduct -> productMapper.toProductResponse(recentlyViewedProduct.getProduct()))
                .collect(Collectors.toList());

        // Xây dựng đối tượng PageResponse
        return PageResponse.<ProductResponse>builder()
                .items(productResponses)
                .currentPage(pageResult.getNumber())
                .pageSize(pageResult.getSize())
                .totalPages(pageResult.getTotalPages())
                .totalElements(pageResult.getTotalElements())
                .build();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void syncViewedProducts(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) return;

        User currentUser = getAuthenticatedUser();

        // Lấy toàn bộ sản phẩm hợp lệ (đã tồn tại)
        List<Product> products = productRepository.findAllById(productIds);

        for (Product product : products) {
            RecentlyViewedProduct existing = repository.findTop1ByUserAndProductOrderByUpdatedAtDesc(currentUser, product);

            if (existing != null) {
                repository.save(existing);
            } else {
                RecentlyViewedProduct newViewed = RecentlyViewedProduct.builder()
                        .user(currentUser)
                        .product(product)
                        .build();
                repository.save(newViewed);
            }
        }
        
        // Sau khi sync, kiểm tra và dọn dẹp nếu vượt quá MAX_ITEMS
        long count = repository.countByUser(currentUser);
        if (count > MAX_ITEMS) {
            int toDelete = (int) (count - MAX_ITEMS);
            repository.deleteOldestByUserId(currentUser.getId(), toDelete);
        }
    }



}


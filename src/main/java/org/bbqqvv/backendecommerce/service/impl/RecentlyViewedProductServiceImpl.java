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
import java.util.Set;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class RecentlyViewedProductServiceImpl implements RecentlyViewedProductService {

    private final RecentlyViewedProductRepository repository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final UserRepository userRepository;
    private final org.springframework.data.redis.core.RedisTemplate<String, String> redisTemplate;

    private static final String REDIS_KEY_PREFIX = "user:recently_viewed:";
    private static final int MAX_ITEMS = 20;

    // 🟢 Lấy user hiện tại (trả về null nếu khách chưa đăng nhập)
    private User getAuthenticatedUser() {
        return SecurityUtils.getCurrentUserLogin()
                .flatMap(userRepository::findByUsername)
                .orElse(null);
    }

    @Override
    @org.springframework.scheduling.annotation.Async
    @org.springframework.transaction.annotation.Transactional
    public void markProductAsViewed(Long productId) {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null) {
            log.debug("Guest user viewed product {}. Not saving to backend.", productId);
            return;
        }

        log.info("Marking product {} as viewed with Redis & Async for user {}", productId, currentUser.getId());
        try {
            String redisKey = REDIS_KEY_PREFIX + currentUser.getId();
            // 1. Ghi vào Redis (ZSET)
            redisTemplate.opsForZSet().add(redisKey, productId.toString(), System.currentTimeMillis());
            // Giới hạn 20 phần tử trong Redis
            redisTemplate.opsForZSet().removeRange(redisKey, 0, -(MAX_ITEMS + 1));

            // 2. Ghi vào DB (để bền vững - Persistent)
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            RecentlyViewedProduct existing = repository.findTop1ByUserAndProductOrderByUpdatedAtDesc(currentUser, product);
            if (existing != null) {
                repository.save(existing);
            } else {
                long count = repository.countByUser(currentUser);
                if (count >= MAX_ITEMS) {
                    repository.deleteOldestByUserId(currentUser.getId(), 1);
                }
                RecentlyViewedProduct recentlyViewedProduct = RecentlyViewedProduct.builder()
                        .user(currentUser)
                        .product(product)
                        .build();
                repository.save(recentlyViewedProduct);
            }
        } catch (Exception e) {
            log.error("Failed to mark product {} for user {}: {}", productId, currentUser.getId(), e.getMessage());
        }
    }

    @Override
    public PageResponse<ProductResponse> getRecentlyViewedProducts(Pageable pageable) {
        User currentUser = getAuthenticatedUser();
        // Nếu là khách, không có dữ liệu trên Backend để trả về
        if (currentUser == null) {
            return PageResponse.<ProductResponse>builder()
                    .items(List.of())
                    .currentPage(pageable.getPageNumber())
                    .pageSize(pageable.getPageSize())
                    .totalElements(0)
                    .build();
        }

        String redisKey = REDIS_KEY_PREFIX + currentUser.getId();

        // Thử lấy từ Redis trước (ZREVRANGE)
        Set<String> productIds = redisTemplate.opsForZSet().reverseRange(redisKey, 
                (long) pageable.getPageNumber() * pageable.getPageSize(), 
                (long) (pageable.getPageNumber() + 1) * pageable.getPageSize() - 1);

        if (productIds != null && !productIds.isEmpty()) {
            log.info("Fetching {} products from Redis for user {}", productIds.size(), currentUser.getId());
            List<Long> ids = productIds.stream().map(Long::valueOf).collect(Collectors.toList());
            List<Product> products = productRepository.findAllById(ids);
            
            // Sắp xếp lại danh sách sản phẩm theo đúng thứ tự trong Redis (vì findAllById không đảm bảo thứ tự)
            Map<Long, Product> productMap = products.stream().collect(Collectors.toMap(Product::getId, p -> p));
            List<ProductResponse> sortedResponses = ids.stream()
                    .filter(productMap::containsKey)
                    .map(id -> productMapper.toProductResponse(productMap.get(id)))
                    .collect(Collectors.toList());

            return PageResponse.<ProductResponse>builder()
                    .items(sortedResponses)
                    .currentPage(pageable.getPageNumber())
                    .pageSize(pageable.getPageSize())
                    .totalElements(redisTemplate.opsForZSet().size(redisKey))
                    .build();
        }

        // Nếu Redis trống, lấy từ DB và nạp vào Redis
        log.info("Redis empty, fetching from DB for user {}", currentUser.getId());
        Page<RecentlyViewedProduct> pageResult = repository.findByUserOrderByUpdatedAtDesc(currentUser, pageable);
        
        // Nạp vào Redis nếu là trang đầu tiên
        if (pageable.getPageNumber() == 0) {
            pageResult.getContent().forEach(rvp -> 
                redisTemplate.opsForZSet().add(redisKey, rvp.getProduct().getId().toString(), 
                    rvp.getUpdatedAt() != null ? rvp.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() : System.currentTimeMillis()));
        }

        List<ProductResponse> productResponses = pageResult.getContent().stream()
                .map(rvp -> productMapper.toProductResponse(rvp.getProduct()))
                .collect(Collectors.toList());

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
        if (currentUser == null) {
            log.warn("Attempted to sync viewed products without authentication.");
            return;
        }

        String redisKey = REDIS_KEY_PREFIX + currentUser.getId();
        long now = System.currentTimeMillis();

        List<Product> products = productRepository.findAllById(productIds);
        log.info("Syncing {} viewed products for user {}", products.size(), currentUser.getId());
        for (Product product : products) {
            // Redis Sync
            redisTemplate.opsForZSet().add(redisKey, product.getId().toString(), now++);
            
            // DB Sync
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
        
        // Cleanup Redis & DB
        redisTemplate.opsForZSet().removeRange(redisKey, 0, -(MAX_ITEMS + 1));
        long count = repository.countByUser(currentUser);
        if (count > MAX_ITEMS) {
            repository.deleteOldestByUserId(currentUser.getId(), (int) (count - MAX_ITEMS));
        }
    }



}


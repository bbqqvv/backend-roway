package org.bbqqvv.backendecommerce.service.impl;

import org.bbqqvv.backendecommerce.config.jwt.SecurityUtils;
import org.bbqqvv.backendecommerce.dto.PageResponse;
import org.bbqqvv.backendecommerce.dto.response.ProductResponse;
import org.bbqqvv.backendecommerce.entity.Product;
import org.bbqqvv.backendecommerce.entity.RecentlyViewedProduct;
import org.bbqqvv.backendecommerce.entity.User;
import org.bbqqvv.backendecommerce.mapper.ProductMapper;
import org.bbqqvv.backendecommerce.repository.ProductRepository;
import org.bbqqvv.backendecommerce.repository.RecentlyViewedProductRepository;
import org.bbqqvv.backendecommerce.repository.UserRepository;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecentlyViewedProductServiceTest {

    @Mock private RecentlyViewedProductRepository repository;
    @Mock private ProductRepository productRepository;
    @Mock private ProductMapper productMapper;
    @Mock private UserRepository userRepository;
    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private ZSetOperations<String, String> zSetOperations;

    @InjectMocks
    private RecentlyViewedProductServiceImpl recentlyViewedProductService;

    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("testuser").build();
        product = Product.builder().id(101L).name("Roway Jeans").build();
    }

    @Test
    @DisplayName("Lưu sản phẩm vừa xem vào Redis và DB")
    void markProductAsViewed_shouldSaveToRedisAndDB() {
        try (var mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("testuser"));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
            when(productRepository.findById(101L)).thenReturn(Optional.of(product));
            when(repository.findTop1ByUserAndProductOrderByUpdatedAtDesc(user, product)).thenReturn(null);

            recentlyViewedProductService.markProductAsViewed(101L);

            verify(zSetOperations).add(contains("user:recently_viewed:1"), eq("101"), anyDouble());
            verify(repository).save(any(RecentlyViewedProduct.class));
        }
    }

    @Test
    @DisplayName("Lấy danh sách sản phẩm vừa xem - Từ Redis")
    void getRecentlyViewedProducts_shouldReturnFromRedis_whenAvailable() {
        Pageable pageable = PageRequest.of(0, 5);
        try (var mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("testuser"));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
            
            Set<String> productIds = new LinkedHashSet<>(List.of("101"));
            when(zSetOperations.reverseRange(anyString(), eq(0L), eq(4L))).thenReturn(productIds);
            when(productRepository.findAllById(anyList())).thenReturn(List.of(product));
            when(productMapper.toProductResponse(any())).thenReturn(new ProductResponse());

            PageResponse<ProductResponse> response = recentlyViewedProductService.getRecentlyViewedProducts(pageable);

            assertThat(response.getItems()).hasSize(1);
            verify(repository, never()).findByUserOrderByUpdatedAtDesc(any(), any());
        }
    }

    @Test
    @DisplayName("Lấy danh sách sản phẩm vừa xem - Từ DB (Redis Miss)")
    void getRecentlyViewedProducts_shouldReturnFromDB_whenRedisEmpty() {
        Pageable pageable = PageRequest.of(0, 5);
        try (var mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("testuser"));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
            
            when(zSetOperations.reverseRange(anyString(), anyLong(), anyLong())).thenReturn(Collections.emptySet());
            
            RecentlyViewedProduct rvp = RecentlyViewedProduct.builder().product(product).build();
            Page<RecentlyViewedProduct> dbPage = new PageImpl<>(List.of(rvp));
            when(repository.findByUserOrderByUpdatedAtDesc(user, pageable)).thenReturn(dbPage);
            when(productMapper.toProductResponse(any())).thenReturn(new ProductResponse());

            PageResponse<ProductResponse> response = recentlyViewedProductService.getRecentlyViewedProducts(pageable);

            assertThat(response.getItems()).hasSize(1);
            verify(repository).findByUserOrderByUpdatedAtDesc(user, pageable);
        }
    }

    @Test
    @DisplayName("Đồng bộ hóa sản phẩm vừa xem từ Client")
    void syncViewedProducts_shouldSyncToRedisAndDB() {
        List<Long> productIds = List.of(101L);
        try (var mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("testuser"));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
            when(productRepository.findAllById(productIds)).thenReturn(List.of(product));

            recentlyViewedProductService.syncViewedProducts(productIds);

            verify(zSetOperations, atLeastOnce()).add(anyString(), eq("101"), anyDouble());
            verify(repository, atLeastOnce()).save(any(RecentlyViewedProduct.class));
        }
    }
}

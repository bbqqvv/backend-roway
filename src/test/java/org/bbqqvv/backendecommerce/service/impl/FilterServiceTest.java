package org.bbqqvv.backendecommerce.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bbqqvv.backendecommerce.dto.PageResponse;
import org.bbqqvv.backendecommerce.dto.response.ProductResponse;
import org.bbqqvv.backendecommerce.entity.Product;
import org.bbqqvv.backendecommerce.mapper.ProductMapper;
import org.bbqqvv.backendecommerce.repository.CategoryRepository;
import org.bbqqvv.backendecommerce.repository.ProductRepository;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilterServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;
    @Mock private ObjectMapper objectMapper;
    @Mock private ProductMapper productMapper;

    @InjectMocks
    private FilterServiceImpl filterService;

    @Test
    @DisplayName("Lấy Filter Options từ Redis (Cache Hit)")
    void getFilterOptions_shouldReturnFromRedis_whenCached() throws JsonProcessingException {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("catalog:filter:options")).thenReturn("{\"colors\":[\"Black\"]}");
        Map<String, Object> cachedOptions = Map.of("colors", List.of("Black"));
        when(objectMapper.readValue(anyString(), any(TypeReference.class))).thenReturn(cachedOptions);

        Map<String, Object> result = filterService.getFilterOptions();

        assertThat(result).containsKey("colors");
        verify(productRepository, never()).findDistinctColors();
    }

    @Test
    @DisplayName("Lấy Filter Options từ DB (Cache Miss)")
    void getFilterOptions_shouldReturnFromDB_whenNotCached() throws JsonProcessingException {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("catalog:filter:options")).thenReturn(null);
        when(productRepository.findDistinctColors()).thenReturn(List.of("Red"));
        when(categoryRepository.findAll()).thenReturn(Collections.emptyList());
        when(productRepository.findMinPrice()).thenReturn(BigDecimal.ZERO);
        when(productRepository.findMaxPrice()).thenReturn(BigDecimal.TEN);

        Map<String, Object> result = filterService.getFilterOptions();

        assertThat(result).containsKey("colors");
        verify(valueOperations).set(eq("catalog:filter:options"), any(), any());
    }

    @Test
    @DisplayName("Filter sản phẩm theo keyword và category")
    void filterProducts_shouldUseSpecification() {
        Pageable pageable = PageRequest.of(0, 10);
        Map<String, String> params = Map.of("keyword", "jeans", "category", "men");
        
        Page<Product> page = new PageImpl<>(List.of(new Product()));
        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(productMapper.toProductResponse(any())).thenReturn(new ProductResponse());

        PageResponse<ProductResponse> response = filterService.filterProducts(params, pageable);

        assertThat(response.getItems()).hasSize(1);
        verify(productRepository).findAll(any(Specification.class), eq(pageable));
    }
}

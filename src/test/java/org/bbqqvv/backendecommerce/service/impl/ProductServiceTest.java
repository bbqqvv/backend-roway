package org.bbqqvv.backendecommerce.service.impl;

import org.bbqqvv.backendecommerce.dto.PageResponse;
import org.bbqqvv.backendecommerce.dto.request.ProductRequest;
import org.bbqqvv.backendecommerce.dto.response.ProductResponse;
import org.bbqqvv.backendecommerce.entity.Category;
import org.bbqqvv.backendecommerce.entity.Product;
import org.bbqqvv.backendecommerce.exception.AppException;
import org.bbqqvv.backendecommerce.exception.codes.ProductErrorCode;
import org.bbqqvv.backendecommerce.mapper.ProductMapper;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private SizeProductRepository sizeProductRepository;
    @Mock private ProductMapper productMapper;
    @Mock private CloudinaryService cloudinaryService;
    @Mock private TagRepository tagRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductRequest productRequest;
    private Category category;

    @BeforeEach
    void setUp() {
        category = Category.builder().id(1L).name("Clothing").slug("clothing").build();
        product = Product.builder()
                .id(1L)
                .name("Roway T-Shirt")
                .productCode("PROD001")
                .slug("roway-t-shirt")
                .category(category)
                .build();

        productRequest = ProductRequest.builder()
                .name("Roway T-Shirt")
                .productCode("PROD001")
                .categoryId(1L)
                .build();
    }

    @Test
    @DisplayName("Lấy sản phẩm theo ID thành công")
    void getProductById_shouldReturnProductResponse_whenProductExists() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productMapper.toProductResponse(any(Product.class))).thenReturn(new ProductResponse());
        when(productRepository.countReviewsByProductId(1L)).thenReturn(5L);

        // Act
        ProductResponse response = productService.getProductById(1L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getReviewCount()).isEqualTo(5);
        verify(productRepository).findById(1L);
    }

    @Test
    @DisplayName("Lấy sản phẩm theo ID thất bại - Không tìm thấy")
    void getProductById_shouldThrowException_whenProductNotFound() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.getProductById(1L))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ProductErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("Tạo mới sản phẩm thành công")
    void createProduct_shouldReturnProductResponse_whenValidRequest() {
        // Arrange
        when(productRepository.existsByProductCode("PROD001")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productMapper.toProduct(any(ProductRequest.class))).thenReturn(product);
        when(productRepository.findSlugsByPattern(anyString())).thenReturn(List.of());
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toProductResponse(any(Product.class))).thenReturn(new ProductResponse());

        // Act
        ProductResponse response = productService.createProduct(productRequest);

        // Assert
        assertThat(response).isNotNull();
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Tạo mới sản phẩm thất bại - Trùng mã sản phẩm")
    void createProduct_shouldThrowException_whenDuplicateCode() {
        // Arrange
        when(productRepository.existsByProductCode("PROD001")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> productService.createProduct(productRequest))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ProductErrorCode.DUPLICATE_PRODUCT_CODE);
    }

    @Test
    @DisplayName("Tìm kiếm sản phẩm thành công")
    void searchProductsByName_shouldReturnPageResponse() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.searchByKeyword(eq("Roway"), any(Pageable.class))).thenReturn(page);
        when(productMapper.toProductResponse(any(Product.class))).thenReturn(new ProductResponse());
        when(productRepository.countReviewsByProductIds(anyList()))
                .thenReturn(java.util.Collections.singletonList(new Object[]{1L, 10L}));

        // Act
        PageResponse<ProductResponse> response = productService.searchProductsByName("Roway", pageable);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(1);
    }

    @Test
    @DisplayName("Tìm kiếm sản phẩm - Không có kết quả")
    void searchProductsByName_shouldReturnEmptyPage_whenNoMatch() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        when(productRepository.searchByKeyword(eq("Unknown"), any(Pageable.class))).thenReturn(Page.empty());

        // Act
        PageResponse<ProductResponse> response = productService.searchProductsByName("Unknown", pageable);

        // Assert
        assertThat(response.getItems()).isEmpty();
        assertThat(response.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("Tìm kiếm sản phẩm - Keyword trống")
    void searchProductsByName_shouldReturnAll_whenKeywordIsEmpty() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.searchByKeyword(eq(""), any(Pageable.class))).thenReturn(page);
        when(productMapper.toProductResponse(any(Product.class))).thenReturn(new ProductResponse());

        // Act
        PageResponse<ProductResponse> response = productService.searchProductsByName("", pageable);

        // Assert
        assertThat(response.getItems()).isNotEmpty();
        verify(productRepository).searchByKeyword(eq(""), any(Pageable.class));
    }
}

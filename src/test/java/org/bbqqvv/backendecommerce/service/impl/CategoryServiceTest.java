package org.bbqqvv.backendecommerce.service.impl;

import org.bbqqvv.backendecommerce.dto.request.CategoryRequest;
import org.bbqqvv.backendecommerce.dto.response.CategoryResponse;
import org.bbqqvv.backendecommerce.entity.Category;
import org.bbqqvv.backendecommerce.exception.AppException;
import org.bbqqvv.backendecommerce.exception.codes.ProductErrorCode;
import org.bbqqvv.backendecommerce.mapper.CategoryMapper;
import org.bbqqvv.backendecommerce.repository.CategoryRepository;
import org.bbqqvv.backendecommerce.service.img.CloudinaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private CategoryMapper categoryMapper;
    @Mock private CloudinaryService cloudinaryService;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;
    private CategoryRequest categoryRequest;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("Books")
                .slug("books")
                .build();

        categoryRequest = CategoryRequest.builder()
                .name("Books")
                .slug("books")
                .build();
    }

    @Test
    @DisplayName("Lấy danh mục theo ID thành công")
    void getCategoryById_shouldReturnCategoryResponse() {
        // Arrange
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryMapper.categoryToCategoryResponse(any())).thenReturn(new CategoryResponse());

        // Act
        CategoryResponse response = categoryService.getCategoryById(1L);

        // Assert
        assertThat(response).isNotNull();
        verify(categoryRepository).findById(1L);
    }

    @Test
    @DisplayName("Lấy danh mục theo ID thất bại - Không tìm thấy")
    void getCategoryById_shouldThrowException_whenNotFound() {
        // Arrange
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> categoryService.getCategoryById(1L))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ProductErrorCode.CATEGORY_NOT_FOUND);
    }

    @Test
    @DisplayName("Tạo danh mục mới thành công")
    void createCategory_shouldReturnCategoryResponse() throws IOException {
        // Arrange
        when(categoryRepository.existsCategoriesByName(anyString())).thenReturn(false);
        when(categoryMapper.categoryRequestToCategory(any())).thenReturn(category);
        when(categoryRepository.save(any())).thenReturn(category);
        when(categoryMapper.categoryToCategoryResponse(any())).thenReturn(new CategoryResponse());

        // Act
        CategoryResponse response = categoryService.createCategory(categoryRequest);

        // Assert
        assertThat(response).isNotNull();
        verify(categoryRepository).save(any(Category.class));
    }
}

package org.bbqqvv.backendecommerce.service.impl;

import org.bbqqvv.backendecommerce.dto.response.BlogCategoryResponse;
import org.bbqqvv.backendecommerce.dto.response.BlogPostResponse;
import org.bbqqvv.backendecommerce.entity.BlogCategory;
import org.bbqqvv.backendecommerce.entity.BlogPost;
import org.bbqqvv.backendecommerce.repository.BlogCategoryRepository;
import org.bbqqvv.backendecommerce.repository.BlogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlogServiceTest {

    @Mock private BlogRepository blogRepository;
    @Mock private BlogCategoryRepository blogCategoryRepository;

    @InjectMocks
    private BlogServiceImpl blogService;

    private BlogPost blogPost;
    private BlogCategory blogCategory;

    @BeforeEach
    void setUp() {
        blogCategory = BlogCategory.builder()
                .id(1L)
                .name("Fashion")
                .slug("fashion")
                .build();

        blogPost = BlogPost.builder()
                .id(1L)
                .title("Trends 2026")
                .slug("trends-2026")
                .category(blogCategory)
                .relatedProducts(new ArrayList<>())
                .date("2026-03-19")
                .build();
    }

    @Test
    @DisplayName("Lấy tất cả danh mục Blog")
    void getAllCategories_shouldReturnList() {
        when(blogCategoryRepository.findAll()).thenReturn(List.of(blogCategory));

        List<BlogCategoryResponse> result = blogService.getAllCategories();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Fashion");
    }

    @Test
    @DisplayName("Lấy tất cả bài viết Blog")
    void getAllPosts_shouldReturnList() {
        when(blogRepository.findAll()).thenReturn(List.of(blogPost));

        List<BlogPostResponse> result = blogService.getAllPosts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Trends 2026");
    }

    @Test
    @DisplayName("Lấy bài viết Blog theo category slug")
    void getPostsByCategory_shouldReturnFilteredList() {
        when(blogRepository.findByCategorySlug("fashion")).thenReturn(List.of(blogPost));

        List<BlogPostResponse> result = blogService.getPostsByCategory("fashion");

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Lấy chi tiết bài viết Blog theo slug")
    void getPostBySlug_shouldReturnResponse_whenFound() {
        when(blogRepository.findBySlug("trends-2026")).thenReturn(Optional.of(blogPost));

        BlogPostResponse result = blogService.getPostBySlug("trends-2026");

        assertThat(result.getTitle()).isEqualTo("Trends 2026");
    }

    @Test
    @DisplayName("Lấy chi tiết bài viết Blog theo slug - Không tìm thấy")
    void getPostBySlug_shouldThrowException_whenNotFound() {
        when(blogRepository.findBySlug("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> blogService.getPostBySlug("unknown"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Blog post not found");
    }
}

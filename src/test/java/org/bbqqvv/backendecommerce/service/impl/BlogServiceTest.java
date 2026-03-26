package org.bbqqvv.backendecommerce.service.impl;

import org.bbqqvv.backendecommerce.dto.PageResponse;
import org.bbqqvv.backendecommerce.dto.request.BlogPostRequest;
import org.bbqqvv.backendecommerce.dto.response.BlogCategoryResponse;
import org.bbqqvv.backendecommerce.dto.response.BlogPostResponse;
import org.bbqqvv.backendecommerce.entity.BlogCategory;
import org.bbqqvv.backendecommerce.entity.BlogPost;
import org.bbqqvv.backendecommerce.repository.BlogCategoryRepository;
import org.bbqqvv.backendecommerce.repository.BlogRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlogServiceTest {

    @Mock private BlogRepository blogRepository;
    @Mock private BlogCategoryRepository blogCategoryRepository;
    @Mock private CloudinaryService cloudinaryService;

    @InjectMocks
    private BlogServiceImpl blogService;

    private BlogPost blogPost;
    private BlogCategory blogCategory;
    private BlogPostRequest blogRequest;

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
                .summary("Summary of trends")
                .content("Detailed content")
                .author("Admin")
                .imageUrl("http://image.url")
                .category(blogCategory)
                .relatedProducts(new ArrayList<>())
                .date("2026-03-26")
                .tags(List.of("fashion", "summer"))
                .build();

        blogRequest = BlogPostRequest.builder()
                .title("New Trend")
                .slug("new-trend")
                .summary("Sum")
                .content("Content")
                .author("Admin")
                .categoryId(1L)
                .imageUrl("http://newimage.url")
                .tags(List.of("tag1"))
                .build();
    }

    // ================= GET METHODS TESTS =================

    @Test
    @DisplayName("Lấy tất cả danh mục Blog")
    void getAllCategories_shouldReturnList() {
        when(blogCategoryRepository.findAll()).thenReturn(List.of(blogCategory));

        List<BlogCategoryResponse> result = blogService.getAllCategories();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Fashion");
    }

    @Test
    @DisplayName("Lấy tất cả bài viết Blog (Phân trang - Không tìm kiếm)")
    void getAllPosts_shouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<BlogPost> mockPage = new PageImpl<>(List.of(blogPost));
        when(blogRepository.findAll(pageable)).thenReturn(mockPage);

        PageResponse<BlogPostResponse> result = blogService.getAllPosts(pageable, "");

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).getTitle()).isEqualTo("Trends 2026");
        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Lấy tất cả bài viết Blog (Phân trang - CÓ tìm kiếm)")
    void getAllPosts_withSearch_shouldReturnFilteredPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<BlogPost> mockPage = new PageImpl<>(List.of(blogPost));
        when(blogRepository.searchByKeyword("Trends", pageable)).thenReturn(mockPage);

        PageResponse<BlogPostResponse> result = blogService.getAllPosts(pageable, "Trends");

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).getTitle()).isEqualTo("Trends 2026");
        verify(blogRepository).searchByKeyword("Trends", pageable);
    }

    @Test
    @DisplayName("Lấy bài viết Blog theo category slug")
    void getPostsByCategory_shouldReturnFilteredList() {
        when(blogRepository.findByCategorySlug("fashion")).thenReturn(List.of(blogPost));

        List<BlogPostResponse> result = blogService.getPostsByCategory("fashion");

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Lấy chi tiết bài viết Blog theo slug - Tìm thấy")
    void getPostBySlug_shouldReturnResponse_whenFound() {
        when(blogRepository.findBySlug("trends-2026")).thenReturn(Optional.of(blogPost));

        BlogPostResponse result = blogService.getPostBySlug("trends-2026");

        assertThat(result.getTitle()).isEqualTo("Trends 2026");
        assertThat(result.getCategory()).isEqualTo("Fashion");
    }

    @Test
    @DisplayName("Lấy chi tiết bài viết Blog theo slug - Không tìm thấy")
    void getPostBySlug_shouldThrowException_whenNotFound() {
        when(blogRepository.findBySlug("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> blogService.getPostBySlug("unknown"))
                .isInstanceOf(RuntimeException.class);
    }

    // ================= CRUD METHODS TESTS =================

    @Test
    @DisplayName("Tạo Blog Post - Thành công")
    void createPost_shouldReturnResponse_whenSuccess() {
        when(blogCategoryRepository.findById(1L)).thenReturn(Optional.of(blogCategory));
        when(blogRepository.save(any(BlogPost.class))).thenAnswer(invocation -> {
            BlogPost savedPost = invocation.getArgument(0);
            savedPost.setId(99L);
            savedPost.setRelatedProducts(new ArrayList<>());
            return savedPost;
        });

        BlogPostResponse result = blogService.createPost(blogRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(99L);
        assertThat(result.getTitle()).isEqualTo("New Trend");
        assertThat(result.getImageUrl()).isEqualTo("http://newimage.url");
        verify(blogRepository, times(1)).save(any(BlogPost.class));
    }

    @Test
    @DisplayName("Tạo Blog Post - Lỗi Category Không Tồn Tại")
    void createPost_shouldThrowException_whenCategoryNotFound() {
        when(blogCategoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> blogService.createPost(blogRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Blog category not found");

        verify(blogRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cập nhật Blog Post - Thành công")
    void updatePost_shouldReturnUpdatedResponse_whenSuccess() {
        when(blogRepository.findById(1L)).thenReturn(Optional.of(blogPost));
        when(blogCategoryRepository.findById(1L)).thenReturn(Optional.of(blogCategory));
        
        when(blogRepository.save(any(BlogPost.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BlogPostResponse result = blogService.updatePost(1L, blogRequest);

        assertThat(result.getTitle()).isEqualTo("New Trend");
        assertThat(result.getImageUrl()).isEqualTo("http://newimage.url");
        verify(blogRepository, times(1)).save(any(BlogPost.class));
    }

    @Test
    @DisplayName("Cập nhật Blog Post - Lỗi Post Không Tồn Tại")
    void updatePost_shouldThrowException_whenPostNotFound() {
        when(blogRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> blogService.updatePost(1L, blogRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Blog post not found");

        verify(blogRepository, never()).save(any());
    }

    @Test
    @DisplayName("Xóa Blog Post - Thành công")
    void deletePost_shouldDelete_whenSuccess() {
        when(blogRepository.findById(1L)).thenReturn(Optional.of(blogPost));

        blogService.deletePost(1L);

        verify(blogRepository, times(1)).delete(blogPost);
    }

    @Test
    @DisplayName("Xóa Blog Post - Lỗi Không Tồn Tại")
    void deletePost_shouldThrowException_whenNotFound() {
        when(blogRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> blogService.deletePost(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Blog post not found");

        verify(blogRepository, never()).delete(any());
    }
}

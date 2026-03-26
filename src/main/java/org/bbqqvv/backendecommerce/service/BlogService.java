package org.bbqqvv.backendecommerce.service;

import org.bbqqvv.backendecommerce.dto.PageResponse;
import org.bbqqvv.backendecommerce.dto.request.BlogPostRequest;
import org.bbqqvv.backendecommerce.dto.response.BlogCategoryResponse;
import org.bbqqvv.backendecommerce.dto.response.BlogPostResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

import org.bbqqvv.backendecommerce.dto.request.BlogCategoryRequest;

public interface BlogService {
    List<BlogCategoryResponse> getAllCategories();
    BlogCategoryResponse createCategory(BlogCategoryRequest request);
    BlogCategoryResponse updateCategory(Long id, BlogCategoryRequest request);
    void deleteCategory(Long id);

    PageResponse<BlogPostResponse> getAllPosts(Pageable pageable, String search);
    List<BlogPostResponse> getPostsByCategory(String categorySlug);
    BlogPostResponse getPostBySlug(String slug);
    BlogPostResponse createPost(BlogPostRequest request);
    BlogPostResponse updatePost(Long id, BlogPostRequest request);
    void deletePost(Long id);
}

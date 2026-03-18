package org.bbqqvv.backendecommerce.service;

import org.bbqqvv.backendecommerce.dto.response.BlogCategoryResponse;
import org.bbqqvv.backendecommerce.dto.response.BlogPostResponse;
import java.util.List;

public interface BlogService {
    List<BlogCategoryResponse> getAllCategories();
    List<BlogPostResponse> getAllPosts();
    List<BlogPostResponse> getPostsByCategory(String categorySlug);
    BlogPostResponse getPostBySlug(String slug);
}

package org.bbqqvv.backendecommerce.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.bbqqvv.backendecommerce.dto.ApiResponse;
import org.bbqqvv.backendecommerce.dto.response.BlogCategoryResponse;
import org.bbqqvv.backendecommerce.dto.response.BlogPostResponse;
import org.bbqqvv.backendecommerce.service.BlogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/blogs")
@RequiredArgsConstructor
@Tag(name = "Blog API", description = "Endpoints for managing blog categories and posts")
public class BlogController {

    private final BlogService blogService;

    @GetMapping("/categories")
    @Operation(summary = "Get all blog categories")
    public ApiResponse<List<BlogCategoryResponse>> getAllCategories() {
        return ApiResponse.success(blogService.getAllCategories());
    }

    @GetMapping
    @Operation(summary = "Get all blog posts")
    public ApiResponse<List<BlogPostResponse>> getAllPosts() {
        return ApiResponse.success(blogService.getAllPosts());
    }

    @GetMapping("/category/{slug}")
    @Operation(summary = "Get blog posts by category slug")
    public ApiResponse<List<BlogPostResponse>> getPostsByCategory(@PathVariable String slug) {
        return ApiResponse.success(blogService.getPostsByCategory(slug));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get blog post by slug")
    public ApiResponse<BlogPostResponse> getPostBySlug(@PathVariable String slug) {
        return ApiResponse.success(blogService.getPostBySlug(slug));
    }
}

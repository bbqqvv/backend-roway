package org.bbqqvv.backendecommerce.controller;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.bbqqvv.backendecommerce.dto.ApiResponse;
import org.bbqqvv.backendecommerce.dto.PageResponse;
import org.bbqqvv.backendecommerce.dto.request.BlogCategoryRequest;
import org.bbqqvv.backendecommerce.dto.request.BlogPostRequest;
import org.bbqqvv.backendecommerce.dto.response.BlogCategoryResponse;
import org.bbqqvv.backendecommerce.dto.response.BlogPostResponse;
import org.bbqqvv.backendecommerce.service.BlogService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PostMapping("/categories")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new blog category")
    public ApiResponse<BlogCategoryResponse> createCategory(@RequestBody @Valid BlogCategoryRequest request) {
        return ApiResponse.success(blogService.createCategory(request));
    }

    @PatchMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a blog category")
    public ApiResponse<BlogCategoryResponse> updateCategory(@PathVariable Long id, @RequestBody @Valid BlogCategoryRequest request) {
        return ApiResponse.success(blogService.updateCategory(id, request));
    }

    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a blog category")
    public ApiResponse<String> deleteCategory(@PathVariable Long id) {
        blogService.deleteCategory(id);
        return ApiResponse.success("Deleted");
    }

    @GetMapping
    @Operation(summary = "Get all blog posts with pagination")
    public ApiResponse<PageResponse<BlogPostResponse>> getAllPosts(
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            @RequestParam(required = false, defaultValue = "") String search) {
        return ApiResponse.success(blogService.getAllPosts(pageable, search));
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

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new blog post")
    public ApiResponse<BlogPostResponse> createPost(@RequestBody @Valid BlogPostRequest request) {
        return ApiResponse.success(blogService.createPost(request));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a blog post")
    public ApiResponse<BlogPostResponse> updatePost(@PathVariable Long id, @RequestBody @Valid BlogPostRequest request) {
        return ApiResponse.success(blogService.updatePost(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a blog post")
    public ApiResponse<String> deletePost(@PathVariable Long id) {
        blogService.deletePost(id);
        return ApiResponse.success("Deleted");
    }

    @PostMapping("/seed")
    @Operation(summary = "Import dummy blog posts for testing")
    public ApiResponse<String> seedDummyBlogs(@RequestParam(defaultValue = "20") int count) {
        blogService.seedDummyBlogs(count);
        return ApiResponse.success("Seeded " + count + " dummy blog posts successfully.");
    }
}

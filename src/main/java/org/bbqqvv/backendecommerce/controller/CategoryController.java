package org.bbqqvv.backendecommerce.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.bbqqvv.backendecommerce.dto.ApiResponse;
import org.bbqqvv.backendecommerce.dto.request.CategoryRequest;
import org.bbqqvv.backendecommerce.dto.response.CategoryResponse;
import org.bbqqvv.backendecommerce.service.CategoryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Category Management", description = "Quản lý danh mục sản phẩm")
public class CategoryController {

    private final CategoryService categoryService;

    // Tạo mới một danh mục
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CategoryResponse> createCategory(@ModelAttribute @Valid CategoryRequest categoryRequest) {
        return ApiResponse.<CategoryResponse>builder()
                .success(true)
                .message("Category created successfully")
                .data(categoryService.createCategory(categoryRequest))
                .build();
    }

    // Lấy danh sách tất cả các danh mục
    @GetMapping
    public ApiResponse<List<CategoryResponse>> getAllCategories() {
        return ApiResponse.<List<CategoryResponse>>builder()
                .success(true)
                .message("List of categories retrieved successfully")
                .data(categoryService.getAllCategories())
                .build();
    }

    // Lấy danh mục theo ID
    @GetMapping("/{id}")
    public ApiResponse<CategoryResponse> getCategoryById(@PathVariable Long id) {
        return ApiResponse.<CategoryResponse>builder()
                .success(true)
                .message("Category details retrieved successfully")
                .data(categoryService.getCategoryById(id))
                .build();
    }

    // Cập nhật thông tin danh mục theo ID
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CategoryResponse> updateCategory(@PathVariable Long id, @ModelAttribute @Valid CategoryRequest categoryRequest) {
        return ApiResponse.<CategoryResponse>builder()
                .success(true)
                .message("Category updated successfully")
                .data(categoryService.updateCategory(id, categoryRequest))
                .build();
    }

    // Xóa một danh mục theo ID
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ApiResponse.<String>builder()
                .success(true)
                .message("Category has been deleted successfully")
                .data("Category deleted")
                .build();
    }
}

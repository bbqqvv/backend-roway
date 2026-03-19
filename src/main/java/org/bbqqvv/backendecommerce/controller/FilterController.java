package org.bbqqvv.backendecommerce.controller;

import lombok.RequiredArgsConstructor;
import org.bbqqvv.backendecommerce.dto.ApiResponse;
import org.bbqqvv.backendecommerce.dto.PageResponse;
import org.bbqqvv.backendecommerce.dto.response.ProductResponse;
import org.bbqqvv.backendecommerce.service.FilterService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/filter")
@RequiredArgsConstructor
public class FilterController {
    private final FilterService filterService;

    @GetMapping("/filter")
    public ApiResponse<PageResponse<ProductResponse>> filterProducts(
            @RequestParam Map<String, String> allParams,
            @PageableDefault(page = 0, size = 9) Pageable pageable) {
        allParams.remove("page");
        allParams.remove("size");
        // Truyền các tham số qua Service để lọc
        PageResponse<ProductResponse> productPage = filterService.filterProducts(allParams, pageable);
        return ApiResponse.success(productPage, "Products filtered successfully");
    }

    @GetMapping("/options")
    public ApiResponse<Map<String, Object>> getFilterOptions() {
        Map<String, Object> options = filterService.getFilterOptions();
        return ApiResponse.success(options, "Filter options fetched successfully");
    }

}

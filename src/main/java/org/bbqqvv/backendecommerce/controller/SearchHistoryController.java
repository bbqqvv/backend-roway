package org.bbqqvv.backendecommerce.controller;

import lombok.RequiredArgsConstructor;
import org.bbqqvv.backendecommerce.dto.ApiResponse;
import org.bbqqvv.backendecommerce.dto.request.SearchHistoryRequest;
import org.bbqqvv.backendecommerce.dto.response.SearchHistoryResponse;
import org.bbqqvv.backendecommerce.service.SearchHistoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search/history")
@RequiredArgsConstructor
public class SearchHistoryController {

    private final SearchHistoryService searchHistoryService;

    // 🟢 Lưu lịch sử tìm kiếm của current user
    @PostMapping
    public ApiResponse<SearchHistoryResponse> saveSearchQuery(@RequestBody SearchHistoryRequest request) {
        return ApiResponse.success(searchHistoryService.saveSearchQuery(request), "Search history saved successfully");
    }

    // 🟢 Lấy lịch sử tìm kiếm của current user
    @GetMapping
    public ApiResponse<List<SearchHistoryResponse>> getUserSearchHistory() {
        return ApiResponse.success(searchHistoryService.getUserSearchHistory(), "User search history retrieved successfully");
    }

    // 🟢 Gợi ý tìm kiếm (autocomplete)
    @GetMapping("/suggestions")
    public ApiResponse<List<SearchHistoryResponse>> getSearchSuggestions(@RequestParam String query) {
        return ApiResponse.success(searchHistoryService.getSearchSuggestions(query), "Search suggestions retrieved successfully");
    }
}

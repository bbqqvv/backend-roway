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
        return ApiResponse.<SearchHistoryResponse>builder()
                .success(true)
                .message("Search history saved successfully")
                .data(searchHistoryService.saveSearchQuery(request))
                .build();
    }

    // 🟢 Lấy lịch sử tìm kiếm của current user
    @GetMapping
    public ApiResponse<List<SearchHistoryResponse>> getUserSearchHistory() {
        return ApiResponse.<List<SearchHistoryResponse>>builder()
                .success(true)
                .message("User search history retrieved successfully")
                .data(searchHistoryService.getUserSearchHistory())
                .build();
    }

    // 🟢 Gợi ý tìm kiếm (autocomplete)
    @GetMapping("/suggestions")
    public ApiResponse<List<SearchHistoryResponse>> getSearchSuggestions(@RequestParam String query) {
        return ApiResponse.<List<SearchHistoryResponse>>builder()
                .success(true)
                .message("Search suggestions retrieved successfully")
                .data(searchHistoryService.getSearchSuggestions(query))
                .build();
    }
}

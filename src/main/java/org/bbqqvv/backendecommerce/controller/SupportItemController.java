package org.bbqqvv.backendecommerce.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.bbqqvv.backendecommerce.dto.ApiResponse;
import org.bbqqvv.backendecommerce.dto.request.SupportItemRequest;
import org.bbqqvv.backendecommerce.dto.response.SupportItemResponse;
import org.bbqqvv.backendecommerce.service.SupportItemsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/support-items")
@RequiredArgsConstructor
public class SupportItemController {

    private final SupportItemsService supportItemsService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<SupportItemResponse> createSupportItem(@ModelAttribute @Valid SupportItemRequest request) {
        SupportItemResponse response = supportItemsService.createSupportItem(request);
        return ApiResponse.<SupportItemResponse>builder()
                .success(true)
                .data(response)
                .message("Support item created successfully.")
                .build();
    }

    @GetMapping
    public ApiResponse<List<SupportItemResponse>> getAllSupportItems() {
        List<SupportItemResponse> responses = supportItemsService.getAllSupportItems();
        return ApiResponse.<List<SupportItemResponse>>builder()
                .success(true)
                .data(responses)
                .message("Support items retrieved successfully.")
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<SupportItemResponse> getSupportItemById(@PathVariable Long id) {
        SupportItemResponse response = supportItemsService.getSupportItemById(id);
        return ApiResponse.<SupportItemResponse>builder()
                .success(true)
                .data(response)
                .message("Support item retrieved successfully.")
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<SupportItemResponse> updateSupportItem(@PathVariable Long id, @ModelAttribute @Valid SupportItemRequest request) {
        SupportItemResponse response = supportItemsService.updateSupportItem(id, request);
        return ApiResponse.<SupportItemResponse>builder()
                .success(true)
                .data(response)
                .message("Support item updated successfully.")
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> deleteSupportItem(@PathVariable Long id) {
        supportItemsService.deleteSupportItem(id);
        return ApiResponse.<String>builder()
                .success(true)
                .data("Support item deleted successfully.")
                .message("The support item has been removed.")
                .build();
    }
}

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
@RequestMapping("/api/v1/support")
@RequiredArgsConstructor
public class SupportItemController {

    private final SupportItemsService supportItemsService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<SupportItemResponse> createSupportItem(@Valid SupportItemRequest request) {
        SupportItemResponse response = supportItemsService.createSupportItem(request);
        return ApiResponse.success(response, "Support item created successfully.");
    }

    @GetMapping
    public ApiResponse<List<SupportItemResponse>> getAllSupportItems() {
        List<SupportItemResponse> responses = supportItemsService.getAllSupportItems();
        return ApiResponse.success(responses, "Support items retrieved successfully.");
    }

    @GetMapping("/{id}")
    public ApiResponse<SupportItemResponse> getSupportItemById(@PathVariable Long id) {
        SupportItemResponse response = supportItemsService.getSupportItemById(id);
        return ApiResponse.success(response, "Support item retrieved successfully.");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<SupportItemResponse> updateSupportItem(@PathVariable Long id, @Valid SupportItemRequest request) {
        SupportItemResponse response = supportItemsService.updateSupportItem(id, request);
        return ApiResponse.success(response, "Support item updated successfully.");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> deleteSupportItem(@PathVariable Long id) {
        supportItemsService.deleteSupportItem(id);
        return ApiResponse.success("The support item has been removed.", "Support item deleted successfully.");
    }
}

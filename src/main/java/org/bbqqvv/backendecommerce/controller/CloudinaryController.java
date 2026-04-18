package org.bbqqvv.backendecommerce.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.bbqqvv.backendecommerce.dto.ApiResponse;
import org.bbqqvv.backendecommerce.dto.request.ImageMetadata;
import org.bbqqvv.backendecommerce.dto.request.ImageRegisterRequest;
import org.bbqqvv.backendecommerce.service.ProductImageService;
import org.bbqqvv.backendecommerce.service.img.CloudinaryService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/cloudinary")
@RequiredArgsConstructor
public class CloudinaryController {

    private final CloudinaryService cloudinaryService;
    private final ProductImageService productImageService;

    @GetMapping("/draft-id")
    public ApiResponse<String> getDraftId() {
        String draftId = UUID.randomUUID().toString();
        return ApiResponse.success(draftId, "Draft ID generated successfully");
    }

    @PostMapping("/register")
    public ApiResponse<String> registerImage(@Valid @RequestBody ImageRegisterRequest request) {
        productImageService.registerImage(request);
        return ApiResponse.success("Image registered successfully", "Registration successful");
    }

    @PostMapping("/register-batch")
    public ApiResponse<String> registerImagesBatch(@Valid @RequestBody List<ImageRegisterRequest> requests) {
        productImageService.registerImagesBatch(requests);
        return ApiResponse.success("Batch of images registered successfully", "Batch registration successful");
    }

    @GetMapping("/staged")
    public ApiResponse<List<ImageMetadata>> getStagedImages(@RequestParam String draftId) {
        return ApiResponse.success(productImageService.getImagesByDraftId(draftId), "Staged images retrieved successfully");
    }

    @DeleteMapping("/unregister")
    public ApiResponse<String> unregisterImage(@RequestParam String publicId) {
        productImageService.unregisterImage(publicId);
        return ApiResponse.success("Image unregistered successfully", "Unregistration successful");
    }

    @GetMapping("/signature")
    public ApiResponse<Map<String, Object>> getUploadSignature(@RequestParam(required = false) String folder) {
        long timestamp = System.currentTimeMillis() / 1000L;
        
        Map<String, Object> params = new HashMap<>();
        params.put("timestamp", timestamp);
        if (folder != null && !folder.isBlank()) {
            params.put("folder", folder);
        }

        String signature = cloudinaryService.generateSignature(params);

        Map<String, Object> response = new HashMap<>();
        response.put("signature", signature);
        response.put("timestamp", timestamp);
        response.put("apiKey", cloudinaryService.getApiKey());
        response.put("cloudName", cloudinaryService.getCloudName());

        return ApiResponse.success(response, "Signature generated successfully");
    }
}

package org.bbqqvv.backendecommerce.controller;

import lombok.RequiredArgsConstructor;
import org.bbqqvv.backendecommerce.dto.ApiResponse;
import org.bbqqvv.backendecommerce.service.img.CloudinaryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cloudinary")
@RequiredArgsConstructor
public class CloudinaryController {

    private final CloudinaryService cloudinaryService;

    @GetMapping("/signature")
    public ApiResponse<Map<String, Object>> getUploadSignature() {
        long timestamp = System.currentTimeMillis() / 1000L;
        
        Map<String, Object> params = new HashMap<>();
        params.put("timestamp", timestamp);
        // Có thể thêm folder hoặc các transformation mặc định vào đây để sign
        // params.put("folder", "products");

        String signature = cloudinaryService.generateSignature(params);

        Map<String, Object> response = new HashMap<>();
        response.put("signature", signature);
        response.put("timestamp", timestamp);
        response.put("apiKey", cloudinaryService.getApiKey());
        response.put("cloudName", cloudinaryService.getCloudName());

        return ApiResponse.success(response, "Signature generated successfully");
    }
}

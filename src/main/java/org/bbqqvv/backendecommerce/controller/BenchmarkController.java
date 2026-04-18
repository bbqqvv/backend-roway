package org.bbqqvv.backendecommerce.controller;

import lombok.RequiredArgsConstructor;
import org.bbqqvv.backendecommerce.dto.ApiResponse;
import org.bbqqvv.backendecommerce.dto.request.ImageMetadata;
import org.bbqqvv.backendecommerce.service.img.CloudinaryService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/benchmarks")
@RequiredArgsConstructor
public class BenchmarkController {

    private final CloudinaryService cloudinaryService;

    /**
     * Strategy B: Upload via Server Proxy (Single)
     */
    @PostMapping("/upload-proxy")
    public ApiResponse<ImageMetadata> uploadProxy(@RequestParam("file") MultipartFile file) {
        ImageMetadata metadata = cloudinaryService.uploadImage(file);
        return ApiResponse.success(metadata, "Uploaded via server proxy");
    }

    /**
     * Strategy B: Upload via Server Proxy (Batch)
     */
    @PostMapping("/upload-proxy-batch")
    public ApiResponse<List<ImageMetadata>> uploadProxyBatch(@RequestParam("files") List<MultipartFile> files) {
        List<ImageMetadata> metadataList = cloudinaryService.uploadImages(files);
        return ApiResponse.success(metadataList, "Batch uploaded via server proxy");
    }
}

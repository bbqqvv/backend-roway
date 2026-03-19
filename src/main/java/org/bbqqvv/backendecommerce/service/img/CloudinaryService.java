package org.bbqqvv.backendecommerce.service.img;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bbqqvv.backendecommerce.dto.request.ImageMetadata;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    /**
     * Upload một ảnh và trả về ImageMetadata (URL + Public ID)
     */
    public ImageMetadata uploadImage(MultipartFile file) {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            return new ImageMetadata(
                uploadResult.get("secure_url").toString(),
                uploadResult.get("public_id").toString()
            );
        } catch (IOException e) {
            log.error("Cloudinary upload failed: {}", e.getMessage());
            throw new RuntimeException("Image upload failed: " + e.getMessage(), e);
        }
    }

    /**
     * Upload nhiều ảnh SONG SONG và trả về danh sách ImageMetadata
     */
    public List<ImageMetadata> uploadImages(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return new ArrayList<>();
        
        List<CompletableFuture<ImageMetadata>> futures = files.stream()
                .map(file -> CompletableFuture.supplyAsync(() -> uploadImage(file)))
                .collect(java.util.stream.Collectors.toList());

        return futures.stream()
                .map(CompletableFuture::join)
                .collect(java.util.stream.Collectors.toList());
    }
    /**
     * Upload file bất kỳ (PDF, DOCX, ZIP...)
     */
    public String uploadFile(MultipartFile file) {
        try {
            Map<String, Object> options = ObjectUtils.asMap(
                    "resource_type", "raw"
            );

            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    options
            );
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new RuntimeException("File upload failed", e);
        }
    }

    /**
     * Tạo signature để Client upload trực tiếp lên Cloudinary (Signed Upload)
     */
    public String generateSignature(Map<String, Object> params) {
        return cloudinary.apiSignRequest(params, cloudinary.config.apiSecret);
    }

    public String getApiKey() {
        return cloudinary.config.apiKey;
    }

    public String getCloudName() {
        return cloudinary.config.cloudName;
    }

    /**
     * Xóa ảnh từ Cloudinary bằng publicId
     */
    public void deleteImage(String publicId) {
        if (publicId == null || publicId.isBlank()) return;
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete image from Cloudinary: " + publicId, e);
        }
    }

    /**
     * Xóa nhiều ảnh từ Cloudinary bằng publicIds
     */
    public void deleteImages(List<String> publicIds) {
        if (publicIds == null || publicIds.isEmpty()) return;
        publicIds.forEach(this::deleteImage);
    }
}

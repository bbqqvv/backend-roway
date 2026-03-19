package org.bbqqvv.backendecommerce.service.impl;

import org.bbqqvv.backendecommerce.exception.codes.*;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.bbqqvv.backendecommerce.dto.request.CategoryRequest;
import org.bbqqvv.backendecommerce.dto.request.ImageMetadata;
import org.bbqqvv.backendecommerce.dto.response.CategoryResponse;
import org.bbqqvv.backendecommerce.entity.Category;
import org.bbqqvv.backendecommerce.entity.SizeCategory;
import org.bbqqvv.backendecommerce.exception.AppException;
import org.bbqqvv.backendecommerce.exception.ErrorCode;
import org.bbqqvv.backendecommerce.mapper.CategoryMapper;
import org.bbqqvv.backendecommerce.mapper.SizeMapper;
import org.bbqqvv.backendecommerce.repository.CategoryRepository;
import org.bbqqvv.backendecommerce.service.CategoryService;
import org.bbqqvv.backendecommerce.service.img.CloudinaryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    CategoryRepository categoryRepository;
    CategoryMapper categoryMapper;
    SizeMapper sizeMapper;
    CloudinaryService cloudinaryService;

    public CategoryServiceImpl(CategoryRepository categoryRepository, CategoryMapper categoryMapper, SizeMapper sizeMapper, CloudinaryService cloudinaryService) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
        this.sizeMapper = sizeMapper;
        this.cloudinaryService = cloudinaryService;
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest categoryRequest) {
        log.info("Creating category: {}", categoryRequest.getName());
        try {
            if (categoryRepository.existsCategoriesByName(categoryRequest.getName())) {
                throw new AppException(ProductErrorCode.CATEGORY_ALREADY_EXISTS);
            }

            Category category = categoryMapper.categoryRequestToCategory(categoryRequest);
            
            // Handle image and publicId
            if (categoryRequest.getImageMetadata() != null && categoryRequest.getImageMetadata().getUrl() != null) {
                category.setImage(categoryRequest.getImageMetadata().getUrl());
                category.setPublicId(categoryRequest.getImageMetadata().getPublicId());
            } else {
                String imageUrl = handleImageUpload(categoryRequest.getImage());
                category.setImage(imageUrl);
            }

            // Manually handle sizes to ensure bidirectional relationship is perfectly established
            if (categoryRequest.getSizes() != null) {
                List<SizeCategory> sizeCategories = categoryRequest.getSizes().stream()
                        .filter(Objects::nonNull)
                        .map(sizeReq -> {
                            SizeCategory size = sizeMapper.toSize(sizeReq);
                            if (size != null) {
                                size.setCategory(category);
                            }
                            return size;
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                category.setSizeCategories(sizeCategories);
            }

            Category savedCategory = categoryRepository.save(category);
            log.info("Category saved with ID: {}", savedCategory.getId());
            return categoryMapper.categoryToCategoryResponse(savedCategory);
        } catch (AppException e) {
            throw e;
        } catch (IOException e) {
            log.error("Lỗi khi tải ảnh", e);
            throw new AppException(InfrastructureAddressErrorCode.IMAGE_UPLOAD_FAILED);
        } catch (Exception e) {
            log.error("Unhandled error creating category", e);
            throw new RuntimeException("Lỗi không xác định khi tạo Category: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest categoryRequest) {
        log.info("Updating category ID: {}", id);
        try {
            Category category = categoryRepository.findById(id)
                    .orElseThrow(() -> new AppException(ProductErrorCode.CATEGORY_NOT_FOUND));

            // Handle image and publicId
            if (categoryRequest.getImageMetadata() != null && categoryRequest.getImageMetadata().getUrl() != null) {
                category.setImage(categoryRequest.getImageMetadata().getUrl());
                category.setPublicId(categoryRequest.getImageMetadata().getPublicId());
            } else if (categoryRequest.getImage() != null && !categoryRequest.getImage().isEmpty()) {
                String imageUrl = handleImageUpload(categoryRequest.getImage());
                category.setImage(imageUrl);
            }

            category.setSlug(categoryRequest.getSlug());
            category.setName(categoryRequest.getName());

            if (categoryRequest.getSizes() != null) {
                List<SizeCategory> sizeCategories = categoryRequest.getSizes().stream()
                        .filter(Objects::nonNull)
                        .map(sizeReq -> {
                            SizeCategory size = sizeMapper.toSize(sizeReq);
                            if (size != null) {
                                size.setCategory(category);
                            }
                            return size;
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                category.setSizeCategories(sizeCategories);
            }

            Category updatedCategory = categoryRepository.save(category);
            return categoryMapper.categoryToCategoryResponse(updatedCategory);
        } catch (AppException e) {
            throw e;
        } catch (IOException e) {
            log.error("Lỗi khi tải ảnh", e);
            throw new AppException(InfrastructureAddressErrorCode.IMAGE_UPLOAD_FAILED);
        } catch (Exception e) {
            log.error("Unhandled error updating category", e);
            throw new RuntimeException("Lỗi không xác định khi cập nhật Category: " + e.getMessage(), e);
        }
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(categoryMapper::categoryToCategoryResponse)
                .orElseThrow(() -> new AppException(ProductErrorCode.CATEGORY_NOT_FOUND));
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::categoryToCategoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ProductErrorCode.CATEGORY_NOT_FOUND));
        categoryRepository.delete(category);
        return true;
    }

    private List<SizeCategory> mapSizeCategories(CategoryRequest categoryRequest, Category category) {
        return categoryRequest.getSizes().stream()
                .map(sizeMapper::toSize)
                .peek(size -> size.setCategory(category))
                .collect(Collectors.toList());
    }

    private String handleImageUpload(MultipartFile image) throws IOException {
        if (image != null && !image.isEmpty()) {
            return cloudinaryService.uploadImage(image).getUrl();
        }
        return null;
    }
}

